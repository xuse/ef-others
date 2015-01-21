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

import java.util.HashMap;

/**
 * 描述一个会话
 * @author Administrator
 */
public class SocketSession{
	private String user;  //IP+端口，记录一个会话的所有者
	private long startTime;//会话开始时间
	private long expireTime;//会话过期时间。(最后一次交流时间加上服务器Session保留时间)
	private HashMap<String,Object> attributes;//会话对象
	
	public String getUser() {
		return user;
	}
	public synchronized void setUser(String user) {
		this.user = user;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public synchronized long getExpireTime() {
		return expireTime;
	}
	public synchronized void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}
	public synchronized HashMap<String, Object> getAttributes() {
		return attributes;
	}
	public synchronized void setAttributes(HashMap<String, Object> attributes) {
		this.attributes = attributes;
	}
}
