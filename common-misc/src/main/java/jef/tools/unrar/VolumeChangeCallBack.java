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
 * the multipart volumn change event calback  
 * @author Joker
 */
public interface VolumeChangeCallBack {
	public static final int RAR_VOL_ASK=0;
	public static final int RAR_VOL_NOTIFY=1;
	
	/**
	 * you should check the RarVolCallBackType,if it is {@link RarVolCallBackType#RAR_VOL_NOTIFY }
	 * you should return NULL for not change the next volumn name.otherwise if it is
	 * {@link RarVolCallBackType#RAR_VOL_ASK } ,you should return the next volumn name or hold on
	 * the thread untill the next default volumn is present 
	 * @param type callback type
	 * @param volename default Required volume 
	 * @return next volumn name
	 */
	VolumnChangeCallbackResult invoke(int type,String volename);
}
