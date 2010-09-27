package com.ubs.punter.jpa;

import java.util.Properties;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
public class Task {
	@Id
	@GeneratedValue
	private long id;
	private int sequence;
	private String name;
	private Properties inputParams;
	private Properties outputParams;
	@ManyToOne
	private Process process;
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
	public Process getProcess() {
		return process;
	}
	public void setProcess(Process process) {
		this.process = process;
	}
	
}
