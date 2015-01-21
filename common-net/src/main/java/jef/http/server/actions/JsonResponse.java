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

import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

import org.easyframe.fastjson.JSON;
import org.easyframe.fastjson.JSONObject;

/**
 * 返回JSON
 * @author jiyi
 *
 */
public class JsonResponse {
	private Boolean success;	//成功标记
	private Object message;//错误对象 
	
	public Object getMessage() {
		return message;
	}

	private Object data;//待传输数据

	private boolean ignorSuccessAndMsg;
	/**
	 * 是否忽略成功标记
	 */
	public boolean isIgnorSuccessAndMsg() {
		return ignorSuccessAndMsg;
	}
	/**
	 * 是否忽略成功标记
	 */
	public void setIgnorSuccessAndMsg(boolean ignorSuccessAndMsg) {
		this.ignorSuccessAndMsg = ignorSuccessAndMsg;
	}
	
	
	public Boolean isSuccess() {
		return success;
	}

	public void setSuccess() {
		this.success = true;
	}

	public void setErrors(Object errors) {
		this.success = false;
		this.message = errors;
	}

	public void setSuccess(Object message) {
		this.success = true;
		this.message = message;
	}
	
	public void setRootDataObject(Object obj) {
		this.data=obj;
	}
	
	public Object getData() {
		return data;
	}
	/**
	 * 设置要返回的内容，该内容将位于返回的json结构的data属性中
	 * @param obj
	 */
	public void setData(Object obj) {
		put("data",obj);
	}

	public void put(String key, Object value) {
		if (data == null){
			data = new JSONObject();
		}else if(!( data instanceof JSONObject)){
			throw new IllegalArgumentException("The return data must be a jsonObject");
		}
		((JSONObject)data).put(key, value);	
	}

	public void output(Writer out) {
		if(ignorSuccessAndMsg){
			JSON.writeJSONStringTo(data, out);
			return;
		}
		if(data==null || data instanceof JSONObject){
			if (success != null) {
				put("success", success);
			}
			if (message != null) {
				put("message", message);
			}
		}
		if(data!=null)
			JSON.writeJSONStringTo(data, out);
	}
	
	public String toString(){
		return JSON.toJSONString(data);
	}

	public void putAll(Map<String, Object> map) {
		for(Entry<String,Object> e:map.entrySet()){
			put(e.getKey(),e.getValue());
		}
	}
}
