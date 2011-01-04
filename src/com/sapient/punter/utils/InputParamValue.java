package com.sapient.punter.utils;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlTransient;

import com.sapient.punter.annotations.InputParam;

public class InputParamValue implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8493039398916116517L;
	@XmlTransient
	private InputParam inputParam;
	private String value;

	public InputParamValue() {
		// TODO Auto-generated constructor stub
	}

	public InputParamValue(InputParam inputParam, String value) {
		this.inputParam = inputParam;
		this.value = value;
	}
	@XmlTransient
	public InputParam getInputParam() {
		return inputParam;
	}

	public String getValue() {
		if (value == null)
			value = "";
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setInputParam(InputParam inputParam) {
		this.inputParam = inputParam;
	}
	@Override
	public String toString() {
		return value;
	}
}
