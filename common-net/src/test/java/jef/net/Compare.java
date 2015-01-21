package jef.net;

public class Compare {
	private String id;
	private String name;
	private Task task=new Task(333);

	public Compare(String string, String string2) {
		this.id=string;
		this.name=string2;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
