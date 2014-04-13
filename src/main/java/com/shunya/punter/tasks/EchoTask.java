package com.shunya.punter.tasks;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.OutputParam;
import com.shunya.punter.annotations.PunterTask;

import java.util.logging.Level;

@PunterTask(author="munishc",name="EchoTask",description="Echo's the input data to SOP",documentation= "src/main/resources/docs/TextSamplerDemoHelp.html")
public class EchoTask extends Tasks {
	@InputParam(required = true,description="enter your name here")
	private String name;

	@OutputParam
	private String outName;

	@Override
	public boolean run() {
		outName = "Hello " + name;
		LOGGER.get().log(Level.INFO, outName);
		return true;
	}
}