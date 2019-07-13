package jef.net.socket.bio;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jef.common.log.LogUtil;
import jef.net.socket.NetEventListener;
import jef.net.socket.SocketServer;
import jef.tools.Exceptions;
import jef.tools.IOUtils;
import jef.tools.security.IChannel;

/**
 * 描述一个到Flex客户端的连接
 * 
 * @author jiyi
 * 
 */
public abstract class AbstractSocketThread extends Thread implements Closeable {
	private Socket s;
	protected  IChannel channel;
	// 记录该会话上一次活动的时间
	protected long lastActionTime=System.currentTimeMillis();
	protected boolean alive=true;
	private final List<NetEventListener<AbstractSocketThread>> listeners=new ArrayList<NetEventListener<AbstractSocketThread>>(4);
	private final Map<String,Object> attributes=new HashMap<String,Object>();
	
	public final boolean isReady(){
		return channel!=null;
	}
	
	/**
	 * 构造会话
	 * 
	 * @param socket
	 */
	public AbstractSocketThread(Socket socket) {
		this.s = socket;
		start();
	}
	
	public void addListener(NetEventListener<AbstractSocketThread> l){
		listeners.add(l);
	}
	
	public boolean removeListener(NetEventListener<IChannel> l){
		return listeners.remove(l);
	}

	/**
	 * 向客户端发送消息
	 * 
	 * @param map
	 */
	public void send(byte[] message) {
		try {
			if(channel!=null){
				channel.write(message);	
			}
			lastActionTime=System.currentTimeMillis();
			for(NetEventListener<AbstractSocketThread> l:listeners){
				l.messageSent(this,message);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 接受消息的线程
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try{
			this.channel=initChannel(s);
		}catch(Exception e){
			Exceptions.log(e);
		}
		if(channel==null){
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		for(NetEventListener<AbstractSocketThread> l:listeners){
			l.channelEstablished(this,channel);
		}
		try {
			while (alive && !s.isClosed()) {
				byte[] data=channel.read();
				lastActionTime=System.currentTimeMillis();
				byte[] result=process(data);
				if(result!=null){
					channel.write(result);
					lastActionTime=System.currentTimeMillis();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(NetEventListener<AbstractSocketThread> l:listeners){
			l.onConnectionClose(this);
		}
		IOUtils.closeQuietly(channel);
		channel=null;
		LogUtil.info("Socket closed." + s.getLocalPort() + "->" + s.getRemoteSocketAddress());
	}

	protected abstract IChannel initChannel(Socket socket)throws IOException ;

	protected abstract byte[] process(byte[] data);

	/**
	 * 调用此方法可以 关闭当前连接
	 */
	public void close() throws IOException {
		alive=false;
	}
	
	public IChannel getChannel(){
		return channel;
	}
	
	public Object getAttribute(String key){
		return attributes.get(key);
	}
	
	public Object removetAttribute(String key){
		return attributes.remove(key);
	}
	
	public void setAttribute(String key,Object value){
		attributes.put(key, value);
	}

	public Socket getSocket(){
		return s;
	}
	
	public String getUser(){
		return SocketServer.getUser(s);
	}
}
