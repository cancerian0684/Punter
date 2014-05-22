package org.shunya.punter.tasks;

import org.apache.commons.io.IOUtils;
import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.OutputParam;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.utils.StringUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

@PunterTask(author = "munishc", name = "HttpGetTask", description = "Plays HTTP GET Request on the given URL.", documentation = "src/main/resources/docs/TextSamplerDemoHelp.html")
public class HttpGetTask extends Tasks {
    private static final int SOCKET_TIMEOUT = 30*1000;
    private static final int READ_TIMEOUT = 60*1000;
    @InputParam(required = true, description = "enter your httpUrl here")
    private String httpUrl;
    @InputParam(required = false)
    private int expectedResponseCode;

    @OutputParam
    private String httpResponse;

    @Override
    public boolean run() {
        boolean status = false;
        LOGGER.get().log(Level.INFO, httpUrl);
        try {
            URL url = new URL(httpUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(SOCKET_TIMEOUT); 
            urlConnection.setReadTimeout(READ_TIMEOUT); 
            int responseCode = urlConnection.getResponseCode();
            httpResponse = IOUtils.toString(urlConnection.getInputStream());
//            LOGGER.get().log(Level.INFO, httpResponse);
            if (expectedResponseCode == responseCode)
                status = true;
            else
                status = false;
        } catch (Exception e) {
            LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
        }
        return status;
    }
}