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

public abstract class AbstractLongEnum {
	protected AbstractLongEnum(long mode){
		value = mode;
	}
	
	protected AbstractLongEnum(AbstractLongEnum mode){
		value = mode.value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)){
			return true;
		}
		
		if (obj instanceof Long){
			return ((Long)obj).longValue() == this.value;
		}
		if (obj instanceof AbstractLongEnum){
			return ((AbstractLongEnum)obj).value == this.value;
		}
		
		return false; 
	}
	
	@Override
	public String toString() {
		return Long.toString(value);
	}
	
    public boolean hasFlag(long flags){
    	return (value & flags) == value;
    }
	
	public long value;
}
