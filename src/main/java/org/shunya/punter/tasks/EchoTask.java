package org.shunya.punter.tasks;

import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.OutputParam;
import org.shunya.punter.annotations.PunterTask;

@PunterTask(author="munishc",name="EchoTask",description="Echo's the input data to SOP",documentation= "src/main/resources/docs/TextSamplerDemoHelp.html")
public class EchoTask extends Tasks {
	@InputParam(required = true,description="enter your name here")
	private String name;

	@OutputParam
	private String outName;

	@Override
	public boolean run() {
		outName = "Hello " + name;
		LOGGER.get().info(outName);
		return true;
	}
}