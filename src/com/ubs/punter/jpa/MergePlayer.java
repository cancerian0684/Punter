/*
 * RetrievePlayer.java
 *
 * Copyright 2007 Sun Microsystems, Inc. ALL RIGHTS RESERVED Use of 
 * this software is authorized pursuant to the terms of the license 
 * found at http://developers.sun.com/berkeley_license.html.
 *

 */

package com.ubs.punter.jpa;

import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author John O'Conner
 *
 */
public class MergePlayer {
    
    /**
     * Creates a new instance of RetrievePlayer
     */
    public MergePlayer() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // Create the EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("league");
        EntityManager em = emf.createEntityManager();
        
        Player p = em.find(Player.class, 1L);
        em.clear();
        // p is now detached
        Team t = new Team("Ventura Surfers", "National");
        p.setTeam(t);
        em.getTransaction().begin();
        Player managedPlayer = em.merge(p);
        em.getTransaction().commit();
        System.out.println(p.toString());
        
        
        em.close();
        emf.close();
        
    }
}
