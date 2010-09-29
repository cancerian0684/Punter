package com.sapient.punter.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class ProcessHistory {
	@Id
	@GeneratedValue
	private long id;
	private String name;
	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date finishTime;
	@OneToMany(cascade={CascadeType.PERSIST,CascadeType.REMOVE,CascadeType.REFRESH},mappedBy="processHistory")
	private List<TaskHistory> taskHistoryList;
	@ManyToOne
	private ProcessDao process;
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
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getFinishTime() {
		return finishTime;
	}
	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}
	public List<TaskHistory> getTaskHistoryList() {
		return taskHistoryList;
	}
	public void setTaskHistoryList(List<TaskHistory> taskHistoryList) {
		this.taskHistoryList = taskHistoryList;
	}
	public ProcessDao getProcess() {
		return process;
	}
	public void setProcess(ProcessDao process) {
		this.process = process;
	}
	
}
