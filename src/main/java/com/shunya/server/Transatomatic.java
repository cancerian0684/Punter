package com.shunya.server;

import org.hibernate.Session;

public interface Transatomatic {
    public void run(UnitOfWork unitOfWork);

    public static interface UnitOfWork {
        public void run(Session session);
    }
}
