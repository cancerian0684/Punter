package com.sapient.server;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static junit.framework.Assert.assertEquals;

public class PunterHttpServerTest {
    private static PunterHttpServer httpServer;

    @BeforeClass
    public static void startServer() throws IOException {
        httpServer = new PunterHttpServer();
        System.out.println("server started.");
    }

    @AfterClass
    public static void stopServer() {
        httpServer.stop();
    }

    @Test
    public void ShouldUploadFileToServer() throws IOException {
        File file = new File("e:/Movies/Himalaya.avi");
        URL url = new URL("http://localhost:8080/file/"+file.getName());
        FileInputStream fileInputStream = new FileInputStream(file);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setAllowUserInteraction(false);
        conn.setUseCaches(false);
        conn.setChunkedStreamingMode(1024*1024);
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
        assertEquals("File Received.", buf.toString());
    }

    @Test
    public void ShouldDownloadFileFromServer() throws IOException {
        URL url = new URL("http://localhost:8080/data/index.html");
        java.net.URLConnection conn = url.openConnection();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        IOUtils.copy(conn.getInputStream(), output);
        assertEquals(673, output.size());
    }
}