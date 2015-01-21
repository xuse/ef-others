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

import java.util.List;

public class MTable<T> extends AbstractInputModel implements InputModel<T>{
	private T data;
	private List<T> tableData;
	private String[] columnNames;
	private int[] columnWidths;
	private String[] columnValues;
	private int width;
	private int height;

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public List<T> getTableData() {
		return tableData;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String... columnNames) {
		this.columnNames = columnNames;
	}

	public int[] getColumnWidths() {
		return columnWidths;
	}

	public void setColumnWidths(int... columnWidths) {
		this.columnWidths = columnWidths;
	}

	public String[] getColumnValues() {
		return columnValues;
	}

	public void setColumnValues(String... columnValues) {
		this.columnValues = columnValues;
	}

	public void setTableData(List<T> tableData) {
		this.tableData = tableData;
	}

	public MTable(String label,List<T> tableData) {
		super(label);
		this.tableData=tableData;
	}
	
	public void setColumns(String[] n,int[] w,String[] v){
		this.columnNames=n;
		this.columnWidths=w;
		this.columnValues=v;
	}

	
	public T get() {
		return this.data;
	}

	
	public void set(T data) {
		this.data=data;
	}
}
