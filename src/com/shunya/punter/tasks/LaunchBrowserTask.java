package com.shunya.punter.tasks;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.OutputParam;
import com.shunya.punter.annotations.PunterTask;
import com.shunya.punter.utils.StringUtils;

@PunterTask(author="munishc",name="LaunchBrowserTask",description="Launch the System default browser fpr the given URL.",documentation= "docs/TextSamplerDemoHelp.html")
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