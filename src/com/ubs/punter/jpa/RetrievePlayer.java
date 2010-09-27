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
 */
public class RetrievePlayer {
    
    /**
     * Creates a new instance of RetrievePlayer
     */
    public RetrievePlayer() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String aTeamName = "Los Angeles Dodgers";
        
        // Create the EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("league");
        EntityManager em = emf.createEntityManager();
        
        Query q = em.createQuery("select c from Player c where c.team.teamName = :name");
        q.setParameter("name", aTeamName);
        List<Player> playerList = q.getResultList();

        for(Player p : playerList) {
            System.out.println(p.toString());
        }
        
        em.close();
        emf.close();
         
    }
    
}
