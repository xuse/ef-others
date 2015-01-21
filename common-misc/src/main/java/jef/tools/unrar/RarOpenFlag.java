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

public class RarOpenFlag extends AbstractLongEnum{
	
	/**
	 * Volume attribute (archive volume) 
	 */
	public final static RarOpenFlag Volume = new RarOpenFlag(1);
	/**
	 * Archive comment present
	 */
	public final static RarOpenFlag Comment = new RarOpenFlag(2);
	/**
	 * Archive lock attribute
	 */	
	public final static RarOpenFlag Lock = new RarOpenFlag(4);
    /**
     * Solid attribute (solid archive)
     */
	public final static RarOpenFlag Solid = new RarOpenFlag(8);
    /**
     * New volume naming scheme ('volname.partN.rar')
     */
	public final static RarOpenFlag Volume_naming_scheme = new RarOpenFlag(16);
    /**
     * Authenticity information present
     */
	public final static RarOpenFlag Authenticity = new RarOpenFlag(32);
    /**
     * Recovery record present
     */
	public final static RarOpenFlag Recovery = new RarOpenFlag(64);
    /**
     * Block headers are encrypted
     */
	public final static RarOpenFlag Encrypted = new RarOpenFlag(128);
    /**
     * First volume (set only by RAR 3.0 and later)
     * 
     */
	public final static RarOpenFlag First_Volume = new RarOpenFlag(256);
    
	private RarOpenFlag(long mode){
		super(mode);
	}
	  

}
