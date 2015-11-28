package org.shunya.punter.tasks;

import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.utils.StringUtils;

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
        LOGGER.get().info("Uploading File :" + filePath + " To Location : " + server);
        try {
            restClient.fileUpload(server, filePath, name, remotePath);
            status = true;
        } catch (Exception e) {
            LOGGER.get().error(StringUtils.getExceptionStackTrace(e));
        }
        return status;
    }
}