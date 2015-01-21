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
 * 仅用于描述MIME 邮件中的 HTML Part
 * 
 * @author Administrator
 * 
 */
public class HtmlPart extends NormalPart {
	private static final long serialVersionUID = -669806751917976614L;

	public HtmlPart(String content) {
		super("text/html", content);
	}

	/**
	 * @deprecated 请使用{@link HtmlPart}
	 * @param string
	 * @param content
	 */
	@Deprecated
	public HtmlPart(String string, String content) {
		this(content);
	}
}
