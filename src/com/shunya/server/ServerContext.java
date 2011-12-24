package com.shunya.server;

import com.shunya.server.com.shunya.server.model.JPATransatomatic;
import com.shunya.server.com.shunya.server.model.SessionCache;

public class ServerContext {
    private final StaticDaoFacade staticDaoFacade;
    private final SessionFacade sessionFacade;
    private final SessionCache sessionCache;
    private final JPATransatomatic transatomatic;
    private final ServerSettings serverSettings;

    public ServerContext(StaticDaoFacade staticDaoFacade, SessionFacade sessionFacade, SessionCache sessionCache, JPATransatomatic transatomatic, ServerSettings serverSettings) {
        this.staticDaoFacade = staticDaoFacade;
        this.sessionFacade = sessionFacade;
        this.sessionCache = sessionCache;
        this.transatomatic = transatomatic;
        this.serverSettings = serverSettings;
    }

    public StaticDaoFacade getStaticDaoFacade() {
        return staticDaoFacade;
    }

    public SessionFacade getSessionFacade() {
        return sessionFacade;
    }

    public SessionCache getSessionCache() {
        return sessionCache;
    }

    public JPATransatomatic getTransatomatic() {
        return transatomatic;
    }

    public ServerSettings getServerSettings() {
        return serverSettings;
    }
}
