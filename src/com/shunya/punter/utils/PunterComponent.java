package com.shunya.punter.utils;

/**
 * Created by IntelliJ IDEA.
 * User: chandemu
 * Date: 1/31/12
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */
public interface PunterComponent {
    public void startComponent();
    public void stopComponent();
    public boolean isStarted();
}
