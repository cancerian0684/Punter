package org.shunya.punter.tasks;

import org.apache.commons.io.IOUtils;
import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.OutputParam;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.utils.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.logging.Level;

@PunterTask(author = "munishc", name = "HttpGetTask", description = "Plays HTTP GET Request on the given URL.", documentation = "src/main/resources/docs/TextSamplerDemoHelp.html")
public class HttpDownloadTask extends Tasks {
    @InputParam(required = true, description = "enter httpUrl here")
    private String httpUrl;
    @InputParam(required = true)
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