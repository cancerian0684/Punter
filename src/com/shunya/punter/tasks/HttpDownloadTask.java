package com.shunya.punter.tasks;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.OutputParam;
import com.shunya.punter.annotations.PunterTask;
import com.shunya.punter.utils.StringUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

import static junit.framework.Assert.assertEquals;

@PunterTask(author = "munishc", name = "HttpGetTask", description = "Plays HTTP GET Request on the given URL.", documentation = "docs/docs/TextSamplerDemoHelp.html")
public class HttpDownloadTask extends Tasks {
    @InputParam(required = true, description = "enter httpUrl here")
    private String httpUrl;
    @InputParam(required = false)
    private String localPath;

    @OutputParam
    private String httpResponse;

    @Override
    public boolean run() {
        boolean status = false;
        LOGGER.get().log(Level.INFO, httpUrl);
        try {
            URL url = new URL(httpUrl);
            java.net.URLConnection conn = url.openConnection();
            String fileName = httpUrl.substring(httpUrl.lastIndexOf("/"));
            FileOutputStream fileOutputStream = new FileOutputStream(new File(localPath, fileName));
            IOUtils.copy(conn.getInputStream(), fileOutputStream);
            fileOutputStream.close();
            status = true;
        } catch (Exception e) {
            LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
        }
        return status;
    }
}