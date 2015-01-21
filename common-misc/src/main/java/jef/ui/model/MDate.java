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

import java.util.Date;

public class MDate extends AbstractInputModel implements InputModel<Date>{
	private Date date;
	private int width=110;
	private boolean withTime=true;
	
	public boolean isWithTime() {
		return withTime;
	}

	public void setWithTime(boolean withTime) {
		this.withTime = withTime;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public MDate(String label) {
		super(label);
	}

	
	public Date get() {
		return date;
	}

	
	public void set(Date data) {
		this.date=data;
	}
}
