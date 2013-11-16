package com.shunya.punter.tasks;

import com.shunya.kb.jpa.StaticDaoFacadeRemote;
import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.OutputParam;
import com.shunya.punter.annotations.PunterTask;
import com.shunya.punter.utils.StringUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

@PunterTask(author = "munishc", name = "HttpGetTask", description = "Plays HTTP GET Request on the given URL.", documentation = "docs/TextSamplerDemoHelp.html")
public class HttpUploadTask extends Tasks {
    @InputParam(required = true, description = "enter your httpUrl here http://localhost:8080/upload/")
    private String httpUrl;
    @InputParam(required = false, description = "Path of the file to upload e:/test.mp3")
    private String filePath;

    @OutputParam
    private String fileDownloadPath;

    @Override
    public boolean run() {
        boolean status = false;
        LOGGER.get().log(Level.INFO, "Uploading File :" + filePath + " To Location : " + httpUrl);
        try {
            File file = new File(filePath);
            URL url = new URL(httpUrl + file.getName());
            FileInputStream fileInputStream = new FileInputStream(file);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setAllowUserInteraction(false);
            conn.setUseCaches(false);
            conn.setChunkedStreamingMode(1024 * 1024);
            System.setProperty("http.keepAlive", "false");
            OutputStream connOutputStream = conn.getOutputStream();
            IOUtils.copyLarge(fileInputStream, connOutputStream);
            fileInputStream.close();
            connOutputStream.flush();
            connOutputStream.close();
            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuffer buf = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                buf.append(line);
            }
            in.close();
            status = true;
            LOGGER.get().log(Level.INFO, buf.toString());
            fileDownloadPath="http://"+ StaticDaoFacadeRemote.getInstance().getServerHostAddress().getHostAddress()+":"+ StaticDaoFacadeRemote.getInstance().getWebServerPort()+"/uploads/"+file.getName();
        } catch (Exception e) {
            LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
        }
        return status;
    }
}