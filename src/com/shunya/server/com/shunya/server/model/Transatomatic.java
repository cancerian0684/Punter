package com.shunya.server.com.shunya.server.model;

/**
 * Created by IntelliJ IDEA.
 * User: mchan2
 * Date: 12/21/11
 * Time: 9:38 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Transatomatic {
    public void run(UnitOfWork unitOfWork);

    public static interface UnitOfWork {
        public void run();
    }
}
