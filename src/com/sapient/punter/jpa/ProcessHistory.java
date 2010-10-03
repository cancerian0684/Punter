package com.sapient.punter.jpa;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class ProcessHistory implements Serializable{
	@Id
	@GeneratedValue
	private long id;
	private String name;
	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date finishTime;
	@OneToMany(cascade={CascadeType.PERSIST,CascadeType.REMOVE,CascadeType.REFRESH},mappedBy="processHistory",fetch=FetchType.LAZY)
	private List<TaskHistory> taskHistoryList;
	@ManyToOne
	private ProcessDao process;
//	@Basic(optional = false)
//	@Column(nullable = false, columnDefinition = "char(1) default 'A'")
	@Enumerated(EnumType.STRING)
	private RunState runState = RunState.NEW;

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
	public RunState getRunState() {
		return runState;
	}
	public void setRunState(RunState runState) {
		this.runState = runState;
	}
}
