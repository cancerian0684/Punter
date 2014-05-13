package com.shunya.server.model;

import org.hibernate.Session;

public interface Transatomatic {
    public void run(UnitOfWork unitOfWork);

    public static interface UnitOfWork {
        public void run(Session session);
    }
}
