package com.sapient.server;

import com.sapient.kb.jpa.Document;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import sun.net.www.protocol.http.HttpURLConnection;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.Executors;

public class PunterHttpServer {
    public static final String DATA = "/data/";
    public static String root = ".";
    private final HttpServer server;
    private int webServerPort;
    private int maxWebServerThread;
    public static void main(String[] args) throws IOException {
        PunterHttpServer httpServer = new PunterHttpServer();
    }

    public void stop() {
        server.stop(0);
    }

    PunterHttpServer() throws IOException {
        webServerPort = ServerSettings.getInstance().getWebServerPort();
        maxWebServerThread = ServerSettings.getInstance().getMaxWebServerThread();
        server = HttpServer.create(new InetSocketAddress(webServerPort), 0);
        server.createContext(DATA, new MyDataHandler());
        server.createContext("/file/", new MyFileHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.stop(0);
            }
        });
    }
}

class MyFileHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        InputStream inputStream = httpExchange.getRequestBody();
        URI requestURI = httpExchange.getRequestURI();
        String name = requestURI.toASCIIString().substring(requestURI.toASCIIString().lastIndexOf('/'));
        IOUtils.copyLarge(inputStream, new FileOutputStream("e:/" + name));
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
        httpExchange.getResponseBody().write(new String("File Received.").getBytes());
        httpExchange.getResponseBody().close();
        httpExchange.close();
    }
}

class MyDataHandler implements HttpHandler {
    /* mapping of file extensions to content-types */
    static java.util.Hashtable map = new java.util.Hashtable();
    final static int BUF_SIZE = 2048;
    static final byte[] EOL = {(byte) '\r', (byte) '\n'};
    byte[] buf;

    static {
        fillMap();
    }

    MyDataHandler() {
        buf = new byte[BUF_SIZE];
    }

    static void setSuffix(String k, String v) {
        map.put(k, v);
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Request : " + httpExchange.getRemoteAddress() + " -> " + httpExchange.getRequestURI());
        try {
            File targetFile = getFile(getFileNameFromURI(httpExchange), new File(PunterHttpServer.root, getFileNameFromURI(httpExchange)));
            sendFile(httpExchange, targetFile);
        } catch (Exception e) {
            e.printStackTrace();
            send404(httpExchange);
        } finally {
            httpExchange.close();
        }
    }

    private String getFileNameFromURI(HttpExchange httpExchange) {
        URI requestURI = httpExchange.getRequestURI();
        String fname = requestURI.getPath().substring(PunterHttpServer.DATA.length() - 1).replace('/', File.separatorChar);
        if (fname.startsWith(File.separator)) {
            fname = fname.substring(1);
        }
        return fname;
    }

    private void send404(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
        httpExchange.getResponseBody().close();
    }

    private void sendFile(HttpExchange httpExchange, File targetFile) throws IOException {
        String name = targetFile.getName();
        int ind = name.lastIndexOf('.');
        String ct = null;
        if (ind > 0) {
            ct = (String) map.get(name.substring(ind));
        }
        if (ct == null) {
            ct = "unknown/unknown";
        }
        Headers responseHeaders = httpExchange.getResponseHeaders();
        responseHeaders.set("Content-Type", ct);
        responseHeaders.set("Content-length", "" + targetFile.length());
        if (!ct.equalsIgnoreCase("text/html")) {
            responseHeaders.set("Content-Disposition", "attachment; filename=" + name);
        }
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
        OutputStream outputStream = httpExchange.getResponseBody();
        IOUtils.copyLarge(new FileInputStream(targetFile), outputStream);
        outputStream.close();
    }

    private File getFile(String fname, File targetFile) throws IOException {
        if (targetFile.isDirectory()) {
            File ind = new File(targetFile, "index.html");
            if (ind.exists()) {
                targetFile = ind;
            }
        }// Special Document
        else if (isLong(fname)) {
            StaticDaoFacade docService = StaticDaoFacade.getInstance();
            Document doc = new Document();
            doc.setId(Long.parseLong(fname));
            doc = docService.getDocument(doc);
            targetFile = PunterWebDocumentHandler.process(doc);
        }
        return targetFile;
    }

    boolean isLong(String number) {
        try {
            Long.parseLong(number);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    static void fillMap() {
        setSuffix("", "content/unknown");
        setSuffix(".uu", "application/octet-stream");
        setSuffix(".exe", "application/octet-stream");
        setSuffix(".ps", "application/postscript");
        setSuffix(".zip", "application/zip");
        setSuffix(".sh", "application/x-shar");
        setSuffix(".tar", "application/x-tar");
        setSuffix(".snd", "audio/basic");
        setSuffix(".au", "audio/basic");
        setSuffix(".wav", "audio/x-wav");
        setSuffix(".gif", "image/gif");
        setSuffix(".jpg", "image/jpeg");
        setSuffix(".jpeg", "image/jpeg");
        setSuffix(".htm", "text/html");
        setSuffix(".html", "text/html");
        setSuffix(".text", "text/plain");
        setSuffix(".c", "text/plain");
        setSuffix(".cc", "text/plain");
        setSuffix(".c++", "text/plain");
        setSuffix(".h", "text/plain");
        setSuffix(".pl", "text/plain");
        setSuffix(".pdf", "application/pdf");
        setSuffix(".chm", "application/x-chm");
        setSuffix(".doc", "application/msword");
        setSuffix(".rtf", "application/msword");
        setSuffix(".txt", "text/plain");
        setSuffix(".java", "text/plain");
        setSuffix(".jnlp", "application/x-java-jnlp-file");
    }

    void listDirectory(File dir, PrintStream ps) throws IOException {
        ps.println("<TITLE>Directory listing</TITLE><P>\n");
        ps.println("<A HREF=\"..\">Parent Directory</A><BR>\n");
        String[] list = dir.list();
        for (int i = 0; list != null && i < list.length; i++) {
            File f = new File(dir, list[i]);
            if (f.isDirectory()) {
                ps.println("<A HREF=\"" + list[i] + "/\">" + list[i] + "/</A><BR>");
            } else {
                ps.println("<A HREF=\"" + list[i] + "\">" + list[i] + "</A><BR");
            }
        }
        ps.println("<P><HR><BR><I>" + (new Date()) + "</I>");
    }
}