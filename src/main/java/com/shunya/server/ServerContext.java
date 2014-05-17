package com.shunya.server;

public class ServerContext {
    private final HibernateDaoFacade hibernateDaoFacade;
    private final SessionFacade sessionFacade;
    private final JPATransatomatic transatomatic;
    private final ServerSettings serverSettings;

    public ServerContext(HibernateDaoFacade hibernateDaoFacade, SessionFacade sessionFacade, JPATransatomatic transatomatic, ServerSettings serverSettings) {
        this.hibernateDaoFacade = hibernateDaoFacade;
        this.sessionFacade = sessionFacade;
        this.transatomatic = transatomatic;
        this.serverSettings = serverSettings;
    }

    public HibernateDaoFacade getHibernateDaoFacade() {
        return hibernateDaoFacade;
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
