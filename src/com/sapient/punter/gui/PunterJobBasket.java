package com.sapient.punter.gui;

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

    public void addJobToBasket(long processId) {
        System.out.println("Adding processId To Basket = " + processId);
        setChanged();
        notifyObservers(processId);
    }
}
