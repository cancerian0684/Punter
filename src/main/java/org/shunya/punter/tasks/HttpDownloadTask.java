package org.shunya.punter.tasks;

import org.apache.commons.io.IOUtils;
import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.OutputParam;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.utils.StringUtils;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.cert.X509Certificate;

@PunterTask(author = "munishc", name = "HttpGetTask", description = "Plays HTTP GET Request on the given URL.", documentation = "src/main/resources/docs/TextSamplerDemoHelp.html")
public class HttpDownloadTask extends Tasks {
    @InputParam(required = false, description = "enter httpUrl here")
    private String httpUrl;
    @InputParam(required = false, description = "enter httpsUrl here")
    private String httpsUrl;
    @InputParam(required = true)
    private String localPath;
    @InputParam(required = false)
    private String fileName;

    @OutputParam
    private String httpResponse;

    @Override
    public boolean run() {
        boolean status = false;
        LOGGER.get().info(httpUrl == null ? httpsUrl : httpUrl);
        if (fileName == null || fileName.isEmpty()) {
            if (httpsUrl != null && !httpsUrl.isEmpty()) {
                fileName = httpsUrl.substring(httpsUrl.lastIndexOf("/"));
            } else {
                fileName = httpUrl.substring(httpUrl.lastIndexOf("/"));
            }
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(localPath, fileName));) {
            if (httpsUrl != null && !httpsUrl.isEmpty()) {
                // Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                } };
                // Install the all-trusting trust manager
                final SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };

                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

                HttpsURLConnection conn = (HttpsURLConnection) new URL(httpsUrl).openConnection();
                IOUtils.copy(conn.getInputStream(), fileOutputStream);
            } else {
                java.net.URLConnection conn = new URL(httpUrl).openConnection();
                IOUtils.copy(conn.getInputStream(), fileOutputStream);
            }
            status = true;
            httpResponse = "Success";
        } catch (Exception e) {
            LOGGER.get().error(StringUtils.getExceptionStackTrace(e));
            httpResponse = "Fail";
        }
        return status;
    }
}