package com.sapient.punter.tasks;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.annotations.OutputParam;
import com.sapient.punter.annotations.PunterTask;

@PunterTask(author="munishc",name="EchoTask",description="Echo's the input data to SOP")
public class EchoTask extends Tasks {
	@InputParam(required = true)
	private String name;
	@InputParam(required = false)
	private int age;
	@InputParam(required = false)
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
		LOGGER.get().log(Level.WARNING, taskDao.getDescription()+name);
		status=true;
		return status;
	}
}