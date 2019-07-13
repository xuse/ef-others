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

import jef.common.JefException;
import jef.common.log.LogUtil;
import jef.net.socket.message.Message;
import jef.net.socket.message.MessageBuilder;
import jef.net.socket.message.RawMessage;
import jef.net.socket.message.StringMessage;
import jef.tools.Assert;
import jef.tools.Exceptions;


public abstract class SocketAction {
	private SocketServer server;
	
	protected void setServer(SocketServer server) {
		this.server = server;
	}

	//不能抛出任何异常，否则会造成channel始终不能释放，形成内存泄漏
	final void doAction(SocketExchange exchange) {
		try{
			Assert.notNull(exchange);
			Message message=MessageBuilder.build(exchange);
			Message response = execute(message,exchange);
			if(response==null){//不返回任何消息
				response=new RawMessage("");
			}
			returnMsg(exchange,response);
		}catch(RuntimeException e){
			LogUtil.show(e);
			Message response=new StringMessage(e.getMessage());
			returnMsg(exchange,response);
		}catch(JefException e){
			LogUtil.show(e);
			Message response=new StringMessage(e.getMessage());
			returnMsg(exchange,response);
		}catch(Throwable e){//致命异常，不向客户端返回任何内容，输出错误信息后直接关闭连接释放资源
			LogUtil.show(e);
			exchange.close();
		}
	}
	
	//向客户端返回错误信息
	private void returnMsg(SocketExchange exchange, Message response) {
		try {
			exchange.setResponse(response);
		} catch (IOException e) {
			Exceptions.log(e);
			exchange.close();
		}
	}

	protected SocketSession getSession(SocketExchange exchange){
		return server.sessions.get(exchange.getUser());
	}
	
	protected void killSession(SocketExchange exchange){
		server.sessions.remove(exchange.getUser());
	}
	
	protected void shutdownServer(){
		server.setShutdown(true);
	}
	
	abstract protected Message execute (Message message,SocketExchange exchange)throws JefException;
}
