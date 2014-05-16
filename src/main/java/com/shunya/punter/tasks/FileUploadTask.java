package com.shunya.punter.tasks;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.PunterTask;
import com.shunya.punter.utils.StringUtils;

import java.util.logging.Level;

@PunterTask(author = "munishc", name = "FileUploadTask", description = "Plays HTTP GET Request on the given URL.", documentation = "src/main/resources/docs/TextSamplerDemoHelp.html")
public class FileUploadTask extends Tasks {
    @InputParam(required = true, description = "enter your httpUrl here http://localhost:8080/upload/")
    private String server;
    @InputParam(required = false, description = "Path of the file to upload e:/test.mp3")
    private String filePath;
    @InputParam(required = false, description = "Name of the file")
    private String name;
    @InputParam(required = false, description = "Remote Path for the file")
    private String remotePath;

    @Override
    public boolean run() {
        boolean status = false;
        LOGGER.get().log(Level.INFO, "Uploading File :" + filePath + " To Location : " + server);
        try {
            restClient.fileUpload(server, filePath, name, remotePath);
            status = true;
        } catch (Exception e) {
            LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
        }
        return status;
    }
}