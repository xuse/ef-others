package org.easyframe.rmi;

import java.io.Serializable;
import java.util.Date;


public class Foo implements Serializable{
	private int id;
	private String name;
	private Date modified;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
}
