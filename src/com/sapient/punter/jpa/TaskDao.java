package com.sapient.punter.jpa;

import java.io.Serializable;
import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="TASK")
public class TaskDao implements Serializable{
	@Id
	@GeneratedValue
	private long id;
	private int sequence;
	private String name;
	private String className;
	private String description;
	private String author;
	private Properties inputParams;
	private Properties outputParams;
	@ManyToOne
	private ProcessDao process;
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
	public Properties getInputParams() {
		return inputParams;
	}
	public void setInputParams(Properties inputParams) {
		this.inputParams = inputParams;
	}
	public Properties getOutputParams() {
		return outputParams;
	}
	public void setOutputParams(Properties outputParams) {
		this.outputParams = outputParams;
	}
	public ProcessDao getProcess() {
		return process;
	}
	public void setProcess(ProcessDao process) {
		this.process = process;
	}
}
