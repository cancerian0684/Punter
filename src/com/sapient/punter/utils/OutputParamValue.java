package com.sapient.punter.utils;

import java.io.Serializable;

import com.sapient.punter.annotations.OutputParam;

public class OutputParamValue implements Serializable{
private OutputParam inputParam;
private String value;
public OutputParamValue(OutputParam inputParam,String value) {
	this.inputParam=inputParam;
	this.value=value;
}
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
