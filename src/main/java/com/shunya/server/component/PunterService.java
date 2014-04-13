package com.shunya.server.component;

import com.shunya.kb.jpa.StaticDaoFacade;
import com.shunya.kb.jpa.StaticDaoFacadeLocal;
import org.springframework.stereotype.Service;

@Service
public class PunterService {

    public StaticDaoFacade getDaoFacade() {
        return StaticDaoFacadeLocal.getInstance();
    }
}
