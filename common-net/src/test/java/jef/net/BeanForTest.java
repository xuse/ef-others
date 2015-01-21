package jef.net;

import java.util.Date;

public class BeanForTest {
	private int age;
	private Date birthday;
	private String name;
	private BeanForTest parent;
	private BeanForTest[] friends;
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BeanForTest getParent() {
		return parent;
	}
	public void setParent(BeanForTest parent) {
		this.parent = parent;
	}
	public BeanForTest[] getFriends() {
		return friends;
	}
	public void setFriends(BeanForTest[] friends) {
		this.friends = friends;
	}
}
