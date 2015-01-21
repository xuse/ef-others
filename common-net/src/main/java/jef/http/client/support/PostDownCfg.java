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


public class PostDownCfg {
	private int type = TYPE_POST; // 获取类型,0表示默认即采用POST method Download
	
	//符合条件的path。(*)可匹配变量{code}
	private String rawUrl;
	//实际提交地址
	private String action; 
	//提交内容中增加一个名为submit","download"的域(仅为兼容旧版本使用)
	private boolean hasSubmitField;
	//Post请求中，{code}所在的Field Name。(特殊值empty，表示不需要提交codefield,全部交由appendField和value处理)
	private String codeField; 
	// 各种自定义字段(,号分隔)
	private String appendField; 
	// 自定义字段固定值(,号分隔)，可以使用{code}表示变量{code}
	private String appendValue; 

	public static final int TYPE_POST = 0;
	public static final int TYPE_GET_URL_BY_XPAH = 1;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getAppendField() {
		return appendField;
	}

	public void setAppendField(String appendField) {
		this.appendField = appendField;
	}

	public String getAppendValue() {
		return appendValue;
	}

	public void setAppendValue(String appendValue) {
		this.appendValue = appendValue;
	}

	public PostDownCfg() {
	};

	public PostDownCfg(String rawUrl, String action, boolean hasSubmitField, String codeField) {
		this.rawUrl = rawUrl;
		this.action = action;
		this.hasSubmitField = hasSubmitField;
		this.codeField = codeField;
	}

	public final String getAction() {
		return action;
	}

	public final String getCodeField() {
		return codeField;
	}

	public final boolean isHasSubmitField() {
		return hasSubmitField;
	}

	public String getRawUrl() {
		return rawUrl;
	}

	public void setRawUrl(String rawUrl) {
		this.rawUrl = rawUrl;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setHasSubmitField(boolean hasSubmitField) {
		this.hasSubmitField = hasSubmitField;
	}

	public void setCodeField(String codeField) {
		this.codeField = codeField;
	}

	
	public String toString() {
		return "Type:" + type+" Url:" + rawUrl;
	}
}
