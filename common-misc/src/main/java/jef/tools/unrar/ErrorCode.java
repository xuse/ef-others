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

public class ErrorCode{
	public final static int SUCCESS =0;
	public final static int ERAR_END_ARCHIVE =10;
	public final static int ERAR_NO_MEMORY = 11;
	public final static int ERAR_BAD_DATA = 12;
	public final static int ERAR_BAD_ARCHIVE = 13;
	public final static int ERAR_UNKNOWN_FORMAT =  14;
	public final static int ERAR_EOPEN = 15;
	public final static int ERAR_ECREATE =  16;
	public final static int ERAR_ECLOSE =   17;
	public final static int ERAR_EREAD =    18;
	public final static int ERAR_EWRITE =   19;
	public final static int ERAR_SMALL_BUF =20;
	public final static int ERAR_UNKNOWN =  21;
	public final static int ERAR_MISSING_PASSWORD =  22;
}
