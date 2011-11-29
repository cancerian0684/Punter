package com.sapient.punter.gui;

import com.sapient.server.PunterProcessRunMessage;

import java.util.Observable;

public class PunterJobBasket extends Observable {
    private static PunterJobBasket instance;

    private PunterJobBasket() {
    }

    public static PunterJobBasket getInstance() {
        if (instance == null) {
            instance = new PunterJobBasket();
        }
        return instance;
    }

    public void addJobToBasket(PunterProcessRunMessage processRunMessage) {
        System.out.println("Adding processId To Basket = " + processRunMessage.getProcessId());
        setChanged();
        notifyObservers(processRunMessage);
    }
}
