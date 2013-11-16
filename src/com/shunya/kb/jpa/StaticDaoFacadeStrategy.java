package com.shunya.kb.jpa;

public class StaticDaoFacadeStrategy {
    private StaticDaoFacade sdf;

    public StaticDaoFacadeStrategy(Strategy strategy) {
        if (strategy == Strategy.LOCAL) {
            sdf = new StaticDaoFacadeLocal();
        }else{
            sdf = new StaticDaoFacadeRMI();
        }
    }

    public enum Strategy{LOCAL, REMOTE}

    public StaticDaoFacade getInstance() {
        return sdf;
    }

}
