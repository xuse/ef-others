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

public class HttpException extends Exception {
	private static final long serialVersionUID = -285141668292990312L;

	public static final int ERR_UNKNOWN = 999;

	public static final int ERR_CONNECT_REFUSE = 998;

	public static final int ERR_100=100;// Continue

	private int errCode;

	public HttpException(HttpStatus status) {
		super(status.message);
		this.errCode=status.code;
	}
	
	public HttpException(int code, String message) {
		super(message);
		this.errCode = code;
	}

	public HttpException(String message) {
		super(message);
		this.errCode = ERR_UNKNOWN;
	}

	public HttpException(int code) {
		super();
		this.errCode = code;
	}

	public final int getErrCode() {
		return errCode;
	}
}
