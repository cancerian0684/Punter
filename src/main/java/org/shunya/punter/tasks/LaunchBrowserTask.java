package org.shunya.punter.tasks;

import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.OutputParam;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.utils.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@PunterTask(author="munishc",name="LaunchBrowserTask",description="Launch the System default browser fpr the given URL.",documentation= "src/main/resources/docs/TextSamplerDemoHelp.html")
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
			LOGGER.get().warn(StringUtils.getExceptionStackTrace(e));
		} catch (URISyntaxException e) {
			LOGGER.get().error(StringUtils.getExceptionStackTrace(e));
		} 
		return status;
	}
}