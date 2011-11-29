package com.shunya.punter.tasks;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.OutputParam;
import com.shunya.punter.annotations.PunterTask;

@PunterTask(author="munishc",name="EchoTask",description="Echo's the input data to SOP",documentation= "docs/docs/TextSamplerDemoHelp.html")
public class EchoTask extends Tasks {
	@InputParam(required = true,description="enter your name here")
	private String name;
	@InputParam(required = false)
	private int age;
	@InputParam(required = false,description="<html>Enter Age in<br> dd-MMM-yyyy format</html>")
	private Date dob;

	@OutputParam
	private String outName;

	@Override
	public boolean run() {
		boolean status=false;
		outName = "Hello " + name+" "+age+" "+dob;
		LOGGER.get().log(Level.INFO, outName);
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		dob.getDate();
		LOGGER.get().log(Level.WARNING, taskDao.getDescription()+name);
		status=true;
		return status;
	}
}