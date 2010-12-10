package com.sapient.punter.jpa;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.swing.text.Document;

@Entity
@TableGenerator(name="seqGen",table="ID_GEN",pkColumnName="GEN_KEY",valueColumnName="GEN_VALUE",pkColumnValue="SEQ_ID",allocationSize=1)
public class ProcessHistory implements Serializable{
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="seqGen")
	private long id;
	private String name;
	private String username;
	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date finishTime;
	@OneToMany(cascade={CascadeType.PERSIST,CascadeType.REMOVE},mappedBy="processHistory",fetch=FetchType.EAGER)
	private List<TaskHistory> taskHistoryList;
	@ManyToOne
	private ProcessData process;
	@Transient
	private int progress;
	@Transient
	private transient Document logDocument;
//	@Basic(optional = false)
//	@Column(nullable = false, columnDefinition = "char(1) default 'A'")
	@Enumerated(EnumType.STRING)
	private RunState runState = RunState.NEW;
	@Enumerated(EnumType.STRING)
	private RunStatus runStatus = RunStatus.NOT_RUN;
	/*@Version
	@Column(name = "OPT_LOCK")
	private Long version;*/
	private boolean clearAlert=false;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setClearAlert(boolean clearAlert) {
		this.clearAlert = clearAlert;
	}
	public boolean isClearAlert() {
		return clearAlert;
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
	public ProcessData getProcess() {
		return process;
	}
	public void setProcess(ProcessData process) {
		this.process = process;
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
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
	
	public Document getLogDocument() {
		return logDocument;
	}
	public void setLogDocument(Document logDocument) {
		this.logDocument = logDocument;
	}
	
	/*public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}*/
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
		if (!(obj instanceof ProcessHistory))
			return false;
		ProcessHistory other = (ProcessHistory) obj;
		if (id != other.id)
			return false;
		return true;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
}
