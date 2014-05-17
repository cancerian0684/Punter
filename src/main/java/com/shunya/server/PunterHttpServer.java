package com.shunya.server;

import com.shunya.kb.jpa.Document;
import com.shunya.punter.gui.PunterJobBasket;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import sun.net.www.protocol.http.HttpURLConnection;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.Executors;

public class PunterHttpServer {
    public static final String DATA = "/";
    public static final String UPLOADS = "/upload/";
    public static final String PROCESS = "/process/";

    public static String root = ".";
    private HttpServer server;
    private int webServerPort;
    private ServerContext context;

    public void stop() {
        server.stop(0);
    }

    public PunterHttpServer(ServerContext context) {
        this.context = context;
    }

    public void start() throws IOException {
        webServerPort = context.getServerSettings().getWebServerPort();
        server = HttpServer.create(new InetSocketAddress(webServerPort), 0);
        server.createContext(DATA, new MyDataHandler(context.getHibernateDaoFacade()));
        server.createContext(UPLOADS, new MyFileUploadHandler());
        server.createContext(PROCESS, new MyProcessHandler());
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

class MyProcessHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        InputStream inputStream = httpExchange.getRequestBody();
        try {
            URI requestURI = httpExchange.getRequestURI();
            String[] split = requestURI.toASCIIString().split("[/]");
            String hostname = split[2];
            String processId = split[3];
            processId = processId.substring(0, processId.indexOf("?") == -1 ? processId.length() : processId.indexOf("?"));
            PunterProcessRunMessage runMessage = new PunterProcessRunMessage();
            int i = requestURI.toASCIIString().indexOf("?");
            if (i > -1) {
                String searchURL = requestURI.toASCIIString().substring(requestURI.toASCIIString().indexOf("?") + 1);
                System.out.println("Search URL: " + searchURL);
                runMessage.setParams(initMap(searchURL));
            }
            runMessage.setHostname(hostname);
            runMessage.setProcessId(Long.parseLong(processId));
            runMessage.setParams(parseQueryString(requestURI.toASCIIString()));
            PunterJobBasket.getInstance().addJobToBasket(runMessage);
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            httpExchange.getResponseBody().write(new String("Process Submitted Successfully.").getBytes());
            httpExchange.getResponseBody().close();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            send404(httpExchange);
        } finally {
            httpExchange.close();
        }
    }

    public Map initMap(String search) throws UnsupportedEncodingException {
        Map parmsMap = new HashMap<String, String>();
        String params[] = search.split("&");
        for (String param : params) {
            String temp[] = param.split("=");
            parmsMap.put(temp[0], java.net.URLDecoder.decode(temp[1], "UTF-8"));
        }
        return parmsMap;
    }

    private void send404(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
        httpExchange.getResponseBody().close();
    }

    public static Map<String, String> parseQueryString(String data) {
        if (data.indexOf("?") == -1 || data.length() <= (data.indexOf("?") + 1))
            return Collections.emptyMap();
        data = data.substring(data.indexOf("?") + 1);
        Map<String, String> answer = new LinkedHashMap<String, String>();
        for (String parameter : data.split("&")) {
            String[] entities = parameter.split("=");
            try {
                String key = URLDecoder.decode(entities[0], "utf-8");
                if (!answer.containsKey(key)) {
                    answer.put(key, URLDecoder.decode(entities[1], "utf-8"));
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return answer;
    }
}

class MyFileUploadHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        InputStream inputStream = httpExchange.getRequestBody();
        URI requestURI = httpExchange.getRequestURI();
        String name = requestURI.toASCIIString().substring(requestURI.toASCIIString().lastIndexOf('/'));
        File file = new File("uploads");
        file.mkdirs();
        FileOutputStream fileOutputStream = new FileOutputStream(new File(file, name));
        IOUtils.copyLarge(inputStream, fileOutputStream);
        fileOutputStream.close();
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
    private HibernateDaoFacade hibernateDaoFacade;

    static {
        fillMap();
    }

    MyDataHandler(HibernateDaoFacade hibernateDaoFacade) {
        this.hibernateDaoFacade = hibernateDaoFacade;
        buf = new byte[BUF_SIZE];
    }

    static void setSuffix(String k, String v) {
        map.put(k, v);
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Request : " + httpExchange.getRemoteAddress() + " -> " + httpExchange.getRequestURI());
        try {
            File targetFile = getFile(getFileNameFromURI(httpExchange), new File(PunterHttpServer.root, getFileNameFromURI(httpExchange)));
            if (!targetFile.exists()) {
                throw new RuntimeException("File Not Found");
            }
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
        /*try {
            final Map<String, String> urlParameters = getUrlParameters(requestURI.getQuery());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
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
            Document doc = new Document();
            doc.setId(Long.parseLong(fname));
            doc = hibernateDaoFacade.getDocument(doc);
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

    public static Map<String, String> getUrlParameters(String query)
            throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();
        for (String param : query.split("[&]")) {
            String pair[] = param.split("=");
            if (pair.length < 2)
                continue;
            String key = URLDecoder.decode(pair[0].replaceAll("&", ""), "utf-8");
            String value = "";
            if (pair.length > 1) {
                try {
                    value = URLDecoder.decode(pair[1], "utf-8");
                } catch (Exception e) {value = pair[1];}
            }
            if (key != null && !key.trim().isEmpty())
                params.put(key.toLowerCase(), value);
        }
        return params;
    }
}