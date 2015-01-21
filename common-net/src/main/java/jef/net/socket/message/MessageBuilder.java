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
package jef.net.socket.message;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import jef.common.BigDataBuffer;
import jef.net.socket.SocketExchange;
import jef.net.socket.message.FileMessage.FileNamePart;
import jef.tools.IOUtils;
import jef.tools.string.CharsetName;

public class MessageBuilder {
	public static Message build(SocketExchange exchange) throws IOException {
		PushbackInputStream in=new PushbackInputStream(exchange.getBuffer().getAsStream(),16);
		return build(in);

	}

	public static Message build(PushbackInputStream in) throws IOException{
		MessageHeader mh=Message.getMessageHeader(in);
		if(mh==null){
			Message message=new RawMessage(in);
			return message;
		}

		if(mh.isMultiPart()){
			List<Message> parts=new ArrayList<Message>();
			MessageHeader[] partsIndex=getMultiPartIndex(in,mh.getContentLength());
			for(MessageHeader header: partsIndex){
				Message part=createMessage(header,in);
				parts.add(part);
			}
			if(mh.getMessageType()==Message.MULTI_PART_MESSAGE){
				MultiPartMessage msg=new MultiPartMessage();
				msg.addParts(parts);
				return msg;
			}else if(mh.getMessageType()==Message.FILE_MESSAGE){
				FileMessage fm=new FileMessage((FileNamePart)parts.get(0),(StreamMessage)parts.get(1));
				return fm;
			}else{
				throw new IllegalArgumentException("Illegal Message type:"+mh.getMessageType());	
			}
		}else if(mh.isSinglePart()){
			return createMessage(mh, in);
		}else{
			throw new IllegalArgumentException("Illegal Message type:"+mh.getMessageType());
		}
	}



	private static MessageHeader[] getMultiPartIndex(InputStream in,long length) throws IOException {
		List<MessageHeader> mhs=new ArrayList<MessageHeader>();
		byte[] msgHeaderBuf=new byte[Message.LENGTH_MSG_HEADER];
		int count=0;
		while(count<length){
			int n=in.read(msgHeaderBuf);
			if(n<10)throw new IOException("Parse message index error:"+ (count+n));
			mhs.add(MessageHeader.load(msgHeaderBuf, 0));
			count+=n;
		}
		return mhs.toArray(new MessageHeader[]{});
	}

	//从数据流构造消息，接收时使用
	private static Message createMessage(MessageHeader mh, InputStream in) throws IOException {
		if(mh.getMessageType()==Message.STRING_MESSAGE){
			return new StringMessage(IOUtils.toByteArray(in,(int)mh.getContentLength()),CharsetName.getName(mh.getCharset()));
		}else if(mh.getMessageType()==	Message.STREAM_MESSAGE){
			//System.out.println("构造了长度为"+mh.getContentLength()+"的StreamMessage");
			BigDataBuffer bb=IOUtils.asBigDataBuffer(in, mh.getContentLength());
			//System.out.println("结果的得到Buffer长度为"+bb.getLength());
			if(mh.getContentLength()!=bb.length()){
				throw new IOException("Message Transfer error. the expected length is "+ mh.getContentLength() +". but received is "+bb.length());
			}
			if(mh.isZipedStream()){
				File tmpFile=bb.toFile();
				//IOUtils.copyToFolder(tmpFile, "c:/"); //For debug
				StreamMessage sm=new StreamMessage(new GZIPInputStream(new FileInputStream(tmpFile)),-1);
				sm.tmpFile=tmpFile;
				return sm;
			}else{
				StreamMessage sm=new StreamMessage(bb.getAsStream(),bb.length());
				sm.tmpFile=bb.getTmpFile();
				return 	sm;
			}
		}else if(mh.getMessageType()==	Message.SERIALIZABLE_OBJECT_MESSAGE){
			//System.out.println("构造了长度为"+mh.getContentLength()+"的StreamMessage");
			BigDataBuffer bb=IOUtils.asBigDataBuffer(in, mh.getContentLength());
			//System.out.println("结果的得到Buffer长度为"+bb.getLength());
			if(mh.getContentLength()!=bb.length()){
				throw new IOException("Message Transfer error. the expected length is "+ mh.getContentLength() +". but received is "+bb.length());
			}
			if(mh.isZipedStream()){
				File tmpFile=bb.toFile();
				//IOUtils.copyToFolder(tmpFile, "c:/"); //For debug
				StreamMessage sm=new SerializableObjectMessage(new GZIPInputStream(new FileInputStream(tmpFile)),-1);
				sm.tmpFile=tmpFile;
				return sm;
			}else{
				StreamMessage sm=new SerializableObjectMessage(bb.getAsStream(),bb.length());
				sm.tmpFile=bb.getTmpFile();
				return 	sm;
			}
		}else if(mh.getMessageType()==	Message.PART_FILENAME){
			//System.out.print("构造了长度为"+mh.getContentLength()+"的PART_FILENAME");
			return new FileNamePart(IOUtils.toByteArray(in,(int)mh.getContentLength()),CharsetName.getName(mh.getCharset()));
		}else{
			throw new IllegalArgumentException("Illegal Message type:" +mh.getMessageType());			
		}
	}
}
