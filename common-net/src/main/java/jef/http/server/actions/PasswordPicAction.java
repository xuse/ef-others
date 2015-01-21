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
package jef.http.server.actions;

import javax.imageio.stream.MemoryCacheImageOutputStream;

import jef.common.Entry;
import jef.common.MimeTypes;
import jef.http.server.WebExchange;
import jef.image.ImageType;
import jef.image.JefImage;

/**
 * 试验，返回密码图片的Action
 * @author Administrator
 *
 */
public class PasswordPicAction implements HttpAction{
	
	public void doGet(WebExchange exchange) {
		Entry<String,JefImage> result=JefImage.createPasswordImage();
		exchange.setResponseHeader("Content-Type", MimeTypes.getByFileName("a.jpg"));
		exchange.setStatus(200);
		MemoryCacheImageOutputStream out=new MemoryCacheImageOutputStream(exchange.getRawOutput());
		result.getValue().saveAs(out, ImageType.JPEG.name(), 80);
		System.out.println("Password text: "+result.getKey());
	}
	
	public void doPost(WebExchange exchange)  {
		doGet(exchange);
	}
}
