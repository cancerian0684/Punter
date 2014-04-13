package com.shunya.punter.utils;

public class RaceCondition {
    protected long money = 0;

    public void add(long value){
        this.money = this.money + value;
    }

    public static void main(String[] args) {
        RaceCondition myBankAccount = new RaceCondition();

    }

}
