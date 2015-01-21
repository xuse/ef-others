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
package jef.tools.unrar;

/**
 * the return value of the need password callback
 * @author Joker
 *
 */
@SuppressWarnings("unused")
public class NeedPwdCallbackResult {
	/**
	 * extracting fallback for don't konw password
	 */
	public static NeedPwdCallbackResult FALLBACK = new NeedPwdCallbackResult(true);
	/**
	 * 
	 * @param argPassword the password of the archive file
	 */
	public NeedPwdCallbackResult(String argPassword) {
		password = argPassword;
	}
	
	private NeedPwdCallbackResult(boolean argIsfallback) {
		fallback = argIsfallback;
	}

	private boolean fallback;
	private String password;
}
