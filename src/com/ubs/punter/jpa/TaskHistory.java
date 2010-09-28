package com.ubs.punter.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class TaskHistory {
	@Id
	@GeneratedValue
	private long id;
	private int sequence;
	private String logs;
	private boolean status;
	@ManyToOne
	private Task task;
	@ManyToOne
	private ProcessHistory processHistory;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public String getLogs() {
		return logs;
	}
	public void setLogs(String logs) {
		this.logs = logs;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}
	public ProcessHistory getProcessHistory() {
		return processHistory;
	}
	public void setProcessHistory(ProcessHistory processHistory) {
		this.processHistory = processHistory;
	}
	
	
}
