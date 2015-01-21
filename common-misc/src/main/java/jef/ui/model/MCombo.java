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
package jef.ui.model;

public class MCombo extends AbstractInputModel implements InputModel<String>{
	private int width = 80;
	
	public MCombo(String label,String[] selection) {
		super(label);
		this.selection=selection;
	}

	private String data;
	private String[] selection;
	
	public String[] getSelection() {
		return selection;
	}

	
	public String get() {
		return data;
	}

	
	public void set(String data) {
		this.data=data;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}
	
	
}
