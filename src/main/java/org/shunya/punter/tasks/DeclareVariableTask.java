package org.shunya.punter.tasks;

import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.OutputParam;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.utils.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

@PunterTask(author="munishc",name="EchoTask",description="Load process properties into the system.",documentation= "src/main/resources/docs/TextSamplerDemoHelp.html")
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
			LOGGER.get().debug("Loading properties into process.");
			InputStream is = new ByteArrayInputStream(properties.getBytes());  
	        Properties prop = new Properties();  
	        prop.load(is);  
			super.loadSessionVariables((Map) prop);
		} catch (Exception e) {
			LOGGER.get().warn(StringUtils.getExceptionStackTrace(e));
			e.printStackTrace();
		}
		LOGGER.get().info("Properties loaded succesfully into Process.");
		status=true;
		outName="success";
		return status;
	}
}