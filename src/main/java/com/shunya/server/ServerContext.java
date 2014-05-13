package com.shunya.server;

import com.shunya.server.model.JPATransatomatic;

public class ServerContext {
    private final StaticDaoFacade staticDaoFacade;
    private final SessionFacade sessionFacade;
    private final JPATransatomatic transatomatic;
    private final ServerSettings serverSettings;

    public ServerContext(StaticDaoFacade staticDaoFacade, SessionFacade sessionFacade, JPATransatomatic transatomatic, ServerSettings serverSettings) {
        this.staticDaoFacade = staticDaoFacade;
        this.sessionFacade = sessionFacade;
        this.transatomatic = transatomatic;
        this.serverSettings = serverSettings;
    }

    public StaticDaoFacade getStaticDaoFacade() {
        return staticDaoFacade;
    }

    public SessionFacade getSessionFacade() {
        return sessionFacade;
    }

    public JPATransatomatic getTransatomatic() {
        return transatomatic;
    }

    public ServerSettings getServerSettings() {
        return serverSettings;
    }
}
