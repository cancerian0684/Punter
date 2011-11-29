package com.shunya.punter.jpa;

import java.io.Serializable;
import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.shunya.punter.utils.InputParamValue;
import com.shunya.punter.utils.OutputParamValue;

@Entity
@Table(name="TASK")
@TableGenerator(name="seqGen",table="ID_GEN",pkColumnName="GEN_KEY",valueColumnName="GEN_VALUE",pkColumnValue="SEQ_ID",allocationSize=1)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sequence",
    "name",
    "className",
    "description",
    "author",
    "active",
    "inputParams",
    "outputParams"
})
@XmlRootElement
public class TaskData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1907841119637052268L;
	@Id
	@XmlTransient
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
	@XmlTransient
	private ProcessData process;
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
