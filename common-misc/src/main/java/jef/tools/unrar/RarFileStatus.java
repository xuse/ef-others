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

public class RarFileStatus {
	private String arcName;
	private RarOpenMode openMode;
	
	/**
	 * state of the comment,output data
	 */
	private int cmtState;
	/**
	 * file attributes,output data
	 */
	private long flags;
	
	private String comment;

	public RarFileStatus(String argarcName, RarOpenMode argopenMode) {
		this.arcName=argarcName;
		this.openMode=argopenMode;
	}	
	public String getArcName() {
		return this.arcName;
	}
	public RarOpenMode getOpenMode() {
		return this.openMode;
	}
	public long getFlags() {
		return this.flags;
	}
	public int getCmtState() {
		return cmtState;
	}
	public String getComment() {
		return comment;
	}

}
