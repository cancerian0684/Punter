package com.shunya.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class LocalJettyRunner {
    private Server server;

    public void start() throws Exception {
        String webApp = "src/main/webapp";
        server = new Server(9991);

        WebAppContext context = new WebAppContext();
        context.setDescriptor(webApp + "/WEB-INF/web.xml");
        context.setResourceBase("src/main/webapp");
//        context.setWar("C:\\Users\\munichan\\Downloads\\opengrok-0.12-rc4\\lib\\source.war");
        context.setContextPath("/");
        context.setParentLoaderPriority(true);

        server.setHandler(context);
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public static void main(String[] args) throws Exception {
        LocalJettyRunner server = new LocalJettyRunner();
        server.start();
        System.in.read();
        server.stop();
    }
}