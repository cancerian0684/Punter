package com.sapient.punter.jpa;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="PROCESS")
public class ProcessData implements Serializable{
//	@SequenceGenerator(name="Emp_Gen", sequenceName="Emp_Seq", allocationSize=5)
//	@GeneratedValue(generator="Emp_Gen")
	@Id
	@GeneratedValue
	private long id;
	private String name;
	private String description;
	private String comments;
	private Properties inputParams;
	@OneToMany(cascade={CascadeType.PERSIST,CascadeType.REMOVE},mappedBy = "process",fetch=FetchType.LAZY)
	private List<TaskData> taskList;
	@OneToMany(cascade={CascadeType.PERSIST,CascadeType.REMOVE,CascadeType.REFRESH},mappedBy = "process",fetch=FetchType.LAZY)
	private List<ProcessHistory> processHistoryList;
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
	public List<TaskData> getTaskList() {
		return taskList;
	}
	public void setTaskList(List<TaskData> taskList) {
		this.taskList = taskList;
	}
	public List<ProcessHistory> getProcessHistoryList() {
		return processHistoryList;
	}
	public void setProcessHistoryList(List<ProcessHistory> processHistoryList) {
		this.processHistoryList = processHistoryList;
	}
	public Properties getInputParams() {
		return inputParams;
	}
	public void setInputParams(Properties inputParams) {
		this.inputParams = inputParams;
	}
	
}
