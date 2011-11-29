package com.sapient.punter.tasks;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.annotations.OutputParam;
import com.sapient.punter.annotations.PunterTask;
import com.sapient.punter.utils.StringUtils;

@PunterTask(author="munishc",name="LaunchBrowserTask",description="Launch the System default browser fpr the given URL.",documentation= "docs/docs/TextSamplerDemoHelp.html")
public class LaunchBrowserTask extends Tasks {
	@InputParam(required = true,description="Enter the URL to launch")
	private String url;

	@OutputParam
	private String result;

	@Override
	public boolean run() {
		boolean status=false;
		try {
			java.awt.Desktop.getDesktop().browse(new URI(url));
			result=url;
			status=true;
		} catch (IOException e) {
			LOGGER.get().log(Level.WARNING, StringUtils.getExceptionStackTrace(e));
		} catch (URISyntaxException e) {
			LOGGER.get().log(Level.WARNING, StringUtils.getExceptionStackTrace(e));
		} 
		return status;
	}
}