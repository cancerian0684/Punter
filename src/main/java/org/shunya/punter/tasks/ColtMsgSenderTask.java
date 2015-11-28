package org.shunya.punter.tasks;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.OutputParam;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.utils.StringUtils;

import java.io.InputStream;

@PunterTask(author="munishc",name="ColtMsgSenderTask",description="Echo's the input data to SOP",documentation= "src/main/resources/docs/TextSamplerDemoHelp.html")
public class ColtMsgSenderTask extends Tasks {
	@InputParam(required = true,description="enter your name here")
	private String server;
	@InputParam(required = false)
	private String json;

	@OutputParam
	private String response;

	@Override
	public boolean run() {
		boolean status=false;
		HttpClient httpclient = new DefaultHttpClient();
		try {
			HttpPost httppost = new HttpPost(server);
			httppost.setHeader("Accept", "text/html,application/xhtml+xml; charset=utf-8");
			httppost.setHeader("Content-Type", "application/json; charset=utf-8");
			httppost.setEntity(new StringEntity(json, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntity = response.getEntity();
			LOGGER.get().info(response.getStatusLine().toString());
			if (resEntity != null) {
				LOGGER.get().info("Response content length: "+ resEntity.getContentLength());
			}
			InputStream in = resEntity.getContent();
			String res = org.apache.commons.io.IOUtils.toString(in);
			LOGGER.get().info(res);
			status=true;
		} catch (Exception e) {
			LOGGER.get().error(StringUtils.getExceptionStackTrace(e));
		} finally {
			try {
				httpclient.getConnectionManager().shutdown();
			} catch (Exception ignore) {
				ignore.printStackTrace();
				LOGGER.get().error(StringUtils.getExceptionStackTrace(ignore));
			}
		}
		return status;
	}
}