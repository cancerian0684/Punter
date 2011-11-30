package com.shunya.punter.tasks;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.OutputParam;
import com.shunya.punter.annotations.PunterTask;

@PunterTask(author="munishc",name="EchoTask",description="Echo's the input data to SOP",documentation= "docs/TextSamplerDemoHelp.html")
public class EchoTask extends Tasks {
	@InputParam(required = true,description="enter your name here")
	private String name;

	@OutputParam
	private String outName;

	@Override
	public boolean run() {
		outName = "Hello " + name;
		LOGGER.get().log(Level.INFO, outName);
		LOGGER.get().log(Level.WARNING, taskDao.getDescription()+name);
		return true;
	}
}