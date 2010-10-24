package com.sapient.punter.jpa;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import com.sapient.punter.utils.InputParamValue;

@Entity
@Table(name="PROCESS")
@TableGenerator(name="seqGen",table="ID_GEN",pkColumnName="GEN_KEY",valueColumnName="GEN_VALUE",pkColumnValue="SEQ_ID",allocationSize=1)
public class ProcessData implements Serializable{
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="seqGen")
	private long id;
	private String name;
	private String description;
	private String comments;
	private HashMap<String, InputParamValue> inputParams;
	@OneToMany(cascade={CascadeType.REMOVE},mappedBy = "process",fetch=FetchType.LAZY)
	private List<TaskData> taskList;
	@OneToMany(cascade={CascadeType.REMOVE},mappedBy = "process",fetch=FetchType.LAZY)
	private List<ProcessHistory> processHistoryList;
	@Version
	@Column(name = "OPT_LOCK")
	private Long version;
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
	public HashMap<String, InputParamValue> getInputParams() {
		return inputParams;
	}
	public void setInputParams(HashMap<String, InputParamValue> inputParams) {
		this.inputParams = inputParams;
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
		if (!(obj instanceof ProcessData))
			return false;
		ProcessData other = (ProcessData) obj;
		if (id != other.id)
			return false;
		return true;
	}
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
	
}
