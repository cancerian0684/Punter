package com.sapient.punter.tasks;

import java.io.InputStream;
import java.util.logging.Level;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.annotations.OutputParam;
import com.sapient.punter.annotations.PunterTask;
import com.sapient.punter.utils.StringUtils;

@PunterTask(author="munishc",name="ColtMsgSenderTask",description="Echo's the input data to SOP",documentation= "docs/docs/TextSamplerDemoHelp.html")
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
			LOGGER.get().log(Level.INFO, response.getStatusLine().toString());
			if (resEntity != null) {
				LOGGER.get().log(Level.INFO, "Response content length: "+ resEntity.getContentLength());
			}
			InputStream in = resEntity.getContent();
			String res = org.apache.commons.io.IOUtils.toString(in);
			LOGGER.get().log(Level.INFO, res);
			status=true;
		} catch (Exception e) {
			LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
		} finally {
			try {
				httpclient.getConnectionManager().shutdown();
			} catch (Exception ignore) {
				ignore.printStackTrace();
				LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(ignore));
			}
		}
		return status;
	}
}