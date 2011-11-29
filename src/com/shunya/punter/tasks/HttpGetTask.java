package com.shunya.punter.tasks;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.OutputParam;
import com.shunya.punter.annotations.PunterTask;
import org.apache.commons.io.IOUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

@PunterTask(author = "munishc", name = "HttpGetTask", description = "Plays HTTP GET Request on the given URL.", documentation = "docs/docs/TextSamplerDemoHelp.html")
public class HttpGetTask extends Tasks {
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
            int responseCode = urlConnection.getResponseCode();
            httpResponse = IOUtils.toString(urlConnection.getInputStream());
            LOGGER.get().log(Level.INFO, httpResponse);
            if (expectedResponseCode == responseCode)
                status = true;
            else
                status = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }
}