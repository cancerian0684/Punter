package com.shunya.punter.tasks;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.OutputParam;
import com.shunya.punter.annotations.PunterTask;
import com.shunya.punter.utils.StringUtils;

@PunterTask(author="munishc",name="EchoTask",description="Load process properties into the system.",documentation= "docs/TextSamplerDemoHelp.html")
public class DeclareVariableTask extends Tasks {
	private static final long serialVersionUID = 1L;

	@InputParam(required = true,description="enter your name here")
	private String properties;

	@OutputParam
	private String outName;

	@Override
	public boolean run() {
		boolean status=false;
		try {
			LOGGER.get().log(Level.FINE, "Loading properties into process.");
			InputStream is = new ByteArrayInputStream(properties.getBytes());  
	        Properties prop = new Properties();  
	        prop.load(is);  
			super.loadSessionVariables((Map) prop);
		} catch (Exception e) {
			LOGGER.get().log(Level.WARNING, StringUtils.getExceptionStackTrace(e));
			e.printStackTrace();
		}
		LOGGER.get().log(Level.FINE, "Properties loaded succesfully into Process.");
		status=true;
		outName="success";
		return status;
	}
}