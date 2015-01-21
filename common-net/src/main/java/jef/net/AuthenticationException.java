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
package jef.net;


public class AuthenticationException extends Exception{
	private static final long serialVersionUID = -6639136288268307843L;

	/**
     * Constructs a new instance of AuthenticationException using the
     * explanation supplied. All other fields default to null.
     *
     * @param	explanation	A possibly null string containing
     * 				additional detail about this exception.
     * @see java.lang.Throwable#getMessage
     */
    public AuthenticationException(String explanation) {
	super(explanation);
    }

    /**
      * Constructs a new instance of AuthenticationException.
      * All fields are set to null.
      */
    public AuthenticationException() {
	super();
    }

	public AuthenticationException(String string, Throwable e) {
		super(string,e);
	}
}
