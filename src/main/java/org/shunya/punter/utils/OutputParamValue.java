package org.shunya.punter.utils;

import org.shunya.punter.annotations.OutputParam;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

public class OutputParamValue implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6781071351602404185L;
	@XmlTransient
	private OutputParam inputParam;
	private String value;

	public OutputParamValue() {
	}

	public OutputParamValue(OutputParam inputParam, String value) {
		this.inputParam = inputParam;
		this.value = value;
	}
	@XmlTransient
	public OutputParam getInputParam() {
		return inputParam;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setInputParam(OutputParam inputParam) {
		this.inputParam = inputParam;
	}
}
