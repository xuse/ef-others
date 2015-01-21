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

import jef.common.log.LogUtil;
import jef.http.server.PostData;
import jef.http.server.PostData.UploadFile;
import jef.http.server.WebExchange;

/**
 * 关于Action
 * @author Administrator
 *
 */
public class AboutAction implements HttpAction{

	public void doGet(WebExchange exchange){
		exchange.setStatus(200);
		exchange.setResponseHeader("Content-Type", "text/plain");
		exchange.print("JEF Server v1.00\n\n");
	}

	public void doPost(WebExchange exchange){
		exchange.setCharset("GBK");
		PostData content = exchange.getPostdata();
		exchange.println("<script>");
		for(UploadFile file: content.getFormFiles()){
			LogUtil.show("You are uploading file:" + file.getFileName() +" Size:" + file.getTmpFile().length());
			exchange.print("parent.document.getElementById('uploadedfile').innerHTML += '");
			exchange.println("<a href=\"upload/fileid\">"+file.getFileName()+"</a><br/>';");
		}
		LogUtil.show("===== uploaded: =====");
		LogUtil.show(content.getParameterMap());
		exchange.println("</script>");
	}
}
