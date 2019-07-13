/*
 * JEF - Copyright 2009-2010 Jiyi (mr.jiyi@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jef.net.socket;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import jef.common.BigDataBuffer;
import jef.net.socket.message.Message;
import jef.net.socket.message.MessageHeader;
import jef.tools.ArrayUtils;
import jef.tools.IOUtils;

/**
 * 描述一个连接的各种属性
 */
public class SocketExchange {
	//
	static final int BUFFER_SIZE = 1440;

	public static final int READ = 1; // 正在读取客户端数据
	public static final int PROCESSING = 9;// 正在处理消息(不能收，不能写)
	public static final int WRITE = 2; // 正在向客户端发送数据
	public static final int FINISH = 3; // 已经完成
	public static final int CLOSED = 4; // 已经关闭，所有对象释放

	SelectionKey key;
	SocketChannel channel;

	// TODO, 性能优化： 不是用BigDataBuffer来存储消息，而是用MessageBuffer来存储接受中的消息
	BigDataBuffer buffer; // 接收消息时的数据缓存
	Message response; // 返回消息时的数据流
	InputStream responseIn;

	// 用于读写操作的缓冲区，关闭时应当释放
	private ByteBuffer tmpBuf = ByteBuffer.allocate(BUFFER_SIZE);
	private byte[] byteBuf = new byte[BUFFER_SIZE];

	private String ip; // 远程IP
	private int port;// 远程端口
	private int status = READ; // 状态
	private long startTime;// 连接开始时间

	/**
	 * 处理端口读取任务
	 */
	long expectLength = 0;// 本次消息的预期长度 0 未计算 -1 无法计算 -2需要等到索引区全部载入后才能知道 xxx实际长度
	long indexLength = 0;
	int isRawMessage = 0;

	public void processRead() throws IOException {
		if (getStatus() != READ) {
			//在认为消息已经结束的时候又传来新的消息？
			throw new IOException("The bytes received after message ends?");
		}
		// 先将所有就绪数据读入
		int n;
		while ((n = channel.read(tmpBuf)) > 0) {
			tmpBuf.flip();
			buffer.write(tmpBuf);
			tmpBuf.clear();
		}
		// 尝试判断消息类型
		if (isRawMessage == 0 && buffer.length() > 6) {
			byte[] tmp6 = new byte[6];
			buffer.putByte(tmp6, 0, 6);
			if (ArrayUtils.isEquals(tmp6, Message.JEF_MESSAGE_FLAG)) {
				isRawMessage = -1;
			} else {
				isRawMessage = 1;
				expectLength = -1;
			}
		}
		// 尝试判断消息长度
		if (expectLength == 0 && buffer.length() > 16) {
			byte[] tmp10 = new byte[10];
			buffer.putByte(tmp10, 6, 10);
			MessageHeader mh = MessageHeader.load(tmp10, 0);
			if (mh.isMultiPart()) {
				expectLength = -2L;
				indexLength = mh.getContentLength();
			} else if (mh.isSinglePart()) {
				expectLength = mh.getContentLength();
			} else {
				throw new RuntimeException();
			}
		}
		// 尝试读取索引
		if (expectLength == -2 && buffer.length() > indexLength) {
			List<MessageHeader> headers = new ArrayList<MessageHeader>();
			int count = 0;
			byte[] tmp10 = new byte[10];
			while (count * 10 < indexLength) {
				buffer.putByte(tmp10, 16 + 10 * count, 10);
				headers.add(MessageHeader.load(tmp10, 0));
				count++;
			}

			long size = 0;
			for (MessageHeader he : headers) {
				size += he.getContentLength();
			}
			expectLength = size;
		}

		if (expectLength > 0) {// 如果消息长度已经计算出来
			long currentReceived = buffer.length() - indexLength - Message.LENGTH_MSG_FLAG - Message.LENGTH_MSG_HEADER;// 目前接收到的实际消息长度
			//System.out.println("传输进度(Byte):" + currentReceived + "/" + expectLength);
			if (currentReceived >= expectLength) {
				setStatus(PROCESSING);
			}
		} else if (expectLength == -1 && n < BUFFER_SIZE) {
			setStatus(PROCESSING);
		}
		// 目前没有连接等待超时的判断，需要以后添加
	}

	long total = 0;

	public void processWrite() throws IOException {
		if (responseIn!= null) {
			int n = responseIn.read(byteBuf);
			if (n == -1) {
				//System.out.println("在关闭前总共写入了" + total + "字节的数据。");
				closeResponse();
				setStatus(FINISH);
			} else {//因为是非阻塞的，如连续写入过多数据，实际测试发现不会有异常抛出，溢出的数据将会丢失。
				channel.write(ByteBuffer.wrap(byteBuf, 0, n));
				total += n;
			}
		}
	}

	private void closeResponse() {
		IOUtils.closeQuietly(responseIn);
		if(response!=null)response.close();
		responseIn=null;
		response=null;
	}

	/**
	 * 当连接完成后关闭所有
	 */
	public synchronized void close() {
		closeResponse();
		buffer.clear();
		buffer = null;

		IOUtils.closeQuietly(channel);
		channel = null;

		byteBuf = null;
		tmpBuf = null;

		status = CLOSED;// 标记为closed，close后的对象将不能进行任何有意义的操作
	}

	public SocketExchange(SelectionKey key) {
		this.key = key;
		this.channel = (SocketChannel) key.channel();
		Socket socket = channel.socket();
		this.ip = socket.getInetAddress().getHostName();
		this.port = socket.getPort();
		this.startTime = System.currentTimeMillis();
		this.status = READ;
		this.buffer = new BigDataBuffer();// 用于保存已经接收到的数据
	}

	public String getUser() {
		return ip.concat(":").concat(String.valueOf(port));
	}

	public synchronized int getStatus() {
		return status;
	}

	public synchronized void setStatus(int status) {
		this.status = status;
		switch (status) {
		case READ:
			key.interestOps(SelectionKey.OP_READ);
			break;
		case PROCESSING:
			key.interestOps(0);
			break;
		case WRITE:
			key.interestOps(SelectionKey.OP_WRITE);
			break;
		case FINISH:
			IOUtils.closeQuietly(channel);
			key.cancel();
			break;
		case CLOSED:
			if (channel != null && channel.isOpen()) {
				IOUtils.closeQuietly(channel);
			}
			if (key.isValid())
				key.cancel();
			break;
		default:
			IOUtils.closeQuietly(channel);
			key.cancel();
			throw new IllegalStateException("The Exchange was set to unknown format.");
		}
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	protected void setResponse(Message response) throws IOException {
		this.response = response;
		writeHeader(response);
		this.responseIn=response.getContent();
		setStatus(SocketExchange.WRITE);
	}

	public void writeHeader(Message message) throws IOException {
		if (message.getMessageType() == Message.RAW_MESSAGE) {
			return;
		}
		channel.write(ByteBuffer.wrap(message.getFlagAndHeader()));
	}

	public BigDataBuffer getBuffer() {
		return buffer;
	}

	public boolean isFromLocalHost() {
		if (ip.equals("127.0.0.1") || ip.equals("localhost")) {
			return true;
		}
		return false;
	}
}
