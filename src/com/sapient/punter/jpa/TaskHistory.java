package com.sapient.punter.jpa;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity
public class TaskHistory implements Serializable{
	@Id
	@GeneratedValue
	private long id;
	private int sequence;
	@Lob
	private String logs;
	private boolean status;
	@ManyToOne
	private TaskData task;
	@ManyToOne
	private ProcessHistory processHistory;
//	@Basic(optional = false)
//	@Column(nullable = false, columnDefinition = "char(1) default 'A'")
	@Enumerated(EnumType.STRING)
	private RunState runState = RunState.NEW;
	@Enumerated(EnumType.STRING)
	private RunStatus runStatus = RunStatus.NOT_RUN;
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
	public TaskData getTask() {
		return task;
	}
	public void setTask(TaskData task) {
		this.task = task;
	}
	public ProcessHistory getProcessHistory() {
		return processHistory;
	}
	public void setProcessHistory(ProcessHistory processHistory) {
		this.processHistory = processHistory;
	}
	public RunState getRunState() {
		return runState;
	}
	public void setRunState(RunState runState) {
		this.runState = runState;
	}
	public RunStatus getRunStatus() {
		return runStatus;
	}
	public void setRunStatus(RunStatus runStatus) {
		this.runStatus = runStatus;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TaskHistory))
			return false;
		TaskHistory other = (TaskHistory) obj;
		if (id != other.id)
			return false;
		return true;
	}
	@Override
    public String toString() {
        return "com.sapient.punter.jpa.TaskHistory[id=" + id + "]";
    }
}
