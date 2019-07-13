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

import java.util.Arrays;

import jef.tools.ArrayUtils;

/**
 * HTTP状态
 * 记录所有RFC协议已知的http状态
 * @author Administrator
 *
 */
public class HttpStatus{
	private HttpStatus(int i, String string) {
		this.code=i;
		this.message=string;
	}
	int code;
	String message;
	
	public static final HttpStatus get(int httpstatus){
		int index=Arrays.binarySearch(ALL_STATUS_CODE, httpstatus);
		if(index>-1){
			return ALL_STATUS[index];
		}else{
			return null;
		}
	}
	
	public static final HttpStatus STATUS_101=new HttpStatus(101,"Switching protocols");
	public static final HttpStatus STATUS_200=new HttpStatus(200,"OK");  //常见.正确
	public static final HttpStatus STATUS_201=new HttpStatus(201,"Created");
	public static final HttpStatus STATUS_202=new HttpStatus(202,"Accepted");
	public static final HttpStatus STATUS_203=new HttpStatus(203,"Non-Authoritative Information");  //实际使用中没见过
	public static final HttpStatus STATUS_204=new HttpStatus(204,"No Content");
	public static final HttpStatus STATUS_205=new HttpStatus(205,"Reset Content");
	public static final HttpStatus STATUS_206=new HttpStatus(206,"Partial Content");//常见.正确
	public static final HttpStatus STATUS_300=new HttpStatus(300,"Multiple Choices");
	public static final HttpStatus STATUS_301=new HttpStatus(301,"Moved Permanently"); 
	public static final HttpStatus STATUS_302=new HttpStatus(302,"Moved Temporatily");
	public static final HttpStatus STATUS_303=new HttpStatus(303,"See Other");
	public static final HttpStatus STATUS_304=new HttpStatus(304,"Not Modified");
	public static final HttpStatus STATUS_305=new HttpStatus(305,"Use Proxy");  //实际使用中没见过
	public static final HttpStatus STATUS_307=new HttpStatus(307,"Temporary Redirect"); 
	public static final HttpStatus STATUS_400=new HttpStatus(400,"Bad Request"); //常见，但引起此问题的原因太多
	public static final HttpStatus STATUS_401=new HttpStatus(401,"Unauthorized"); 
	public static final HttpStatus STATUS_402=new HttpStatus(402,"Payment Required");
	public static final HttpStatus STATUS_403=new HttpStatus(403,"Forbidden");//常见，但引起此问题的原因太多，某些服务器处于放攻击也会对正常请求报此错误
	public static final HttpStatus STATUS_404=new HttpStatus(404,"Not Found");//常见
	public static final HttpStatus STATUS_405=new HttpStatus(405,"Method Not Allowed");
	public static final HttpStatus STATUS_406=new HttpStatus(406,"Not Acceptable");
	public static final HttpStatus STATUS_407=new HttpStatus(407,"Proxy Authentication Required"); 
	public static final HttpStatus STATUS_408=new HttpStatus(408,"Request Timeout"); 
	public static final HttpStatus STATUS_409=new HttpStatus(409,"Conflict");
	public static final HttpStatus STATUS_410=new HttpStatus(409,"Gone");
	public static final HttpStatus STATUS_411=new HttpStatus(411,"Length Required");
	public static final HttpStatus STATUS_412=new HttpStatus(412,"Precondition Failed");
	public static final HttpStatus STATUS_413=new HttpStatus(413,"Request Entity Too Large");
	public static final HttpStatus STATUS_414=new HttpStatus(414,"Request-URI Too Long");
	public static final HttpStatus STATUS_415=new HttpStatus(415,"Unsupported Media Type");
	public static final HttpStatus STATUS_416=new HttpStatus(416,"Requested Range Not Suitable");
	public static final HttpStatus STATUS_417=new HttpStatus(417,"Expectation Failed");
	public static final HttpStatus STATUS_500=new HttpStatus(500,"Internal Server Error");//常见，但引起此问题的原因太多
	public static final HttpStatus STATUS_501=new HttpStatus(501,"Not Implemented");
	public static final HttpStatus STATUS_502=new HttpStatus(502,"Bad Gateway");
	public static final HttpStatus STATUS_503=new HttpStatus(503,"Service Unavailable");
	public static final HttpStatus STATUS_504=new HttpStatus(504,"Gateway Timeout");
	public static final HttpStatus STATUS_505=new HttpStatus(505,"HTTP Version Not Supported");
	
	public static final HttpStatus CUSTOM_BREAKPOINT_UNSUPPORT=new HttpStatus(600,"The Site not support breakpoint");
	
	static final HttpStatus[] ALL_STATUS=new HttpStatus[] {
		STATUS_101,
		STATUS_200,STATUS_201,STATUS_202,STATUS_203,STATUS_204,STATUS_205,STATUS_206,
		STATUS_300,STATUS_301,STATUS_302,STATUS_303,STATUS_304,STATUS_305,STATUS_307,
		STATUS_400,STATUS_401,STATUS_402,STATUS_403,STATUS_404,STATUS_405,STATUS_406,STATUS_407,
		STATUS_408,STATUS_409,STATUS_410,STATUS_411,STATUS_412,STATUS_413,STATUS_414,STATUS_415,
		STATUS_416,STATUS_417,
		STATUS_500,STATUS_501,STATUS_502,STATUS_503,STATUS_504,STATUS_505
	};
	     
	static final int[] ALL_STATUS_CODE=new int[] {
		101,
		200,201,202,203,204,205,206,
		300,301,302,303,304,305,307,
		400,401,402,403,404,405,406,407,
		408,409,410,411,412,413,414,415,
		416,417,
		500,501,502,503,504,505
	};
	
	//此两种状态表示数据就绪可以读取
	public static final int[] DATA_READY=new int[]{200,206};
	
	//此类状态表示请求无问题，且不需返回数据
	public static final int[] PROCESSED=new int[]{201,202};
	
	//此类状态表示内容被重定向，请到Http头的Location当中去查找重定向后的URL
	//要注意某些网站在发生404 URL后，会直接重定向到一个指定页面
	public static final int[] REDIRECT=new int[]{301,302,303,307};
	
	//即常见的404 not found.
	public static final int NO_DATA_NOT_FOUND= 404;
	
	//由于请求头中包含了If-Modified-Since, if-Not-Match等缓存头，服务器认为本地缓存即可满足请求，要求客户端使用本地缓存 
	public static final int[] NO_DATA_USE_CACHE= new int[]{304};
	                        
	//此类错误表示由于请求格式有问题的错误，应当检查客户端程序
	public static final int[] REQUEST_FORMAT_ERR=new int[]{411,416,413,414};

	//服务器或代理需要认证，需要将密码附到HTTP头当中
	public static final int[] NEED_PASSWORD=new int[]{401,407,402};
	
	public boolean isRedirect(){
		return ArrayUtils.contains(REDIRECT, code);
	}
	public boolean isNotFound(){
		return this.code==NO_DATA_NOT_FOUND;
	}
	public boolean isReady(){
		return ArrayUtils.contains(DATA_READY, code);
	}
	
	public String toString() {
		return "HTTP:"+code+" "+message;
	}
	
}
