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
package jef.http.client.support;

/**
 * 一个纯文本的消息（报文）部分。
 * 内容是纯文本，可以用getContent方法获得
 * @author jiyi
 *
 */
public class NormalPart implements Part {
	private static final long serialVersionUID = -3875129868333764178L;
	private String name;
	private String content;
	private String transferEncode;

	public String getContent() {
		return content;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name=name;
	}

	public NormalPart(String name, String content) {
		this.name = name;
		this.content = content;
	}

	public long getLength() {
		return content.length();
	}

	@Override
	public String toString() {
		return content;
	}

	public String getTransferEncode() {
		return transferEncode;
	}

	public void setTransferEncode(String transferEncode) {
		this.transferEncode = transferEncode;
	}
}
