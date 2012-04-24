package com.shunya.server.com.shunya.server.model;

public interface Transatomatic {
    public void run(UnitOfWork unitOfWork);

    public static interface UnitOfWork {
        public void run();
    }
}
