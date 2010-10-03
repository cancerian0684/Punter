package com.sapient.punter.utils;

import java.io.Serializable;

import com.sapient.punter.annotations.InputParam;

public class InputParamValue implements Serializable{
private InputParam inputParam;
private String value;
public InputParamValue(InputParam inputParam,String value) {
	this.inputParam=inputParam;
	this.value=value;
}
public InputParam getInputParam() {
	return inputParam;
}
public String getValue() {
	return value;
}
public void setValue(String value) {
	this.value = value;
}
public void setInputParam(InputParam inputParam) {
	this.inputParam = inputParam;
}
}
