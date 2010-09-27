package com.ubs.punter.jpa;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Process {
//	@SequenceGenerator(name="Emp_Gen", sequenceName="Emp_Seq", allocationSize=5)
//	@GeneratedValue(generator="Emp_Gen")
	@Id
	@GeneratedValue
	private long id;
	private String name;
	private String description;
	private String comments;
//	@Basic(fetch=FetchType.EAGER)
	@OneToMany(cascade={CascadeType.PERSIST,CascadeType.REMOVE},mappedBy = "process")
	private List<Task> taskList;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public List<Task> getTaskList() {
		return taskList;
	}
	public void setTaskList(List<Task> taskList) {
		this.taskList = taskList;
	}
	
}
