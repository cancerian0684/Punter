package com.sapient.punter.jpa;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import com.sapient.punter.utils.InputParamValue;
import com.sapient.punter.utils.OutputParamValue;

@Entity
@Table(name="TASK")
@TableGenerator(name="seqGen",table="ID_GEN",pkColumnName="GEN_KEY",valueColumnName="GEN_VALUE",pkColumnValue="SEQ_ID",allocationSize=1)
public class TaskData implements Serializable{
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="seqGen")
	private long id;
	private int sequence;
	private String name;
	private String className;
	private String description;
	private String author;
	private boolean active=true;
	private HashMap<String, InputParamValue> inputParams;
	private HashMap<String, OutputParamValue> outputParams;
	@ManyToOne
	private ProcessData process;
	@Version
	@Column(name = "OPT_LOCK")
	private Long version;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public  HashMap<String, InputParamValue> getInputParams() {
		return inputParams;
	}
	public void setInputParams( HashMap<String, InputParamValue> inputParams) {
		this.inputParams = inputParams;
	}
	public HashMap<String,OutputParamValue> getOutputParams() {
		return outputParams;
	}
	public void setOutputParams(HashMap<String,OutputParamValue> outputParams) {
		this.outputParams = outputParams;
	}
	public ProcessData getProcess() {
		return process;
	}
	public void setProcess(ProcessData process) {
		this.process = process;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
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
		if (!(obj instanceof TaskData))
			return false;
		TaskData other = (TaskData) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
