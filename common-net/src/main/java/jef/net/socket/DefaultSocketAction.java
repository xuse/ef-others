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

import java.io.File;

import jef.common.log.LogUtil;
import jef.net.socket.message.FileMessage;
import jef.net.socket.message.Message;
import jef.net.socket.message.SerializableObjectMessage;
import jef.net.socket.message.StreamMessage;
import jef.net.socket.message.StringMessage;
import jef.tools.DateUtils;
import jef.tools.IOUtils;

public class DefaultSocketAction extends SocketAction {
	
	protected Message execute(Message message, SocketExchange exchange) {

		System.out.println(message.getClass().getSimpleName());
		System.out.println("============");
		try {
			if(message instanceof StringMessage){
				System.out.println(message.toString());
			}else if (message instanceof FileMessage) {
				FileMessage fm = (FileMessage) message;
				IOUtils.saveAsFile(new File("c://rec" + fm.getFileName()),fm.getFileContent());
			}else if (message instanceof StreamMessage) {
				System.out.println(IOUtils.asString(message.getContent(),"UTF-8",false));	
			}else if (message instanceof SerializableObjectMessage) {
				SerializableObjectMessage container=(SerializableObjectMessage)message;
				Object obj=container.getObject();
				System.out.println(obj.getClass().getName());
				System.out.println(obj.toString());
			}
		} catch (Exception e) {
			LogUtil.exception(e);
		}
		return new StringMessage(DateUtils.format(System.currentTimeMillis()));
	}

}
