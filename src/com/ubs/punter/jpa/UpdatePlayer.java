/*
 * UpdatePlayer.java
 *
 * Copyright 2007 Sun Microsystems, Inc. ALL RIGHTS RESERVED Use of 
 * this software is authorized pursuant to the terms of the license 
 * found at http://developers.sun.com/berkeley_license.html.
 *
 */

package com.ubs.punter.jpa;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author John O'Conner
 */
public class UpdatePlayer {
    
    /** Creates a new instance of UpdatePlayer */
    public UpdatePlayer() {
    }
    
    
    public static void main(String[] args) {
                String aTeamName = "Los Angeles Dodgers";
        
        // Create the EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("league");
        EntityManager em = emf.createEntityManager();
        
        Query q = em.createQuery("update Player p " +
                "set p.jerseyNumber = (p.jerseyNumber + 1) " +
                "where p.team.teamName = :name");
        q.setParameter("name", aTeamName);
        
        em.getTransaction().begin();
        q.executeUpdate();
        em.getTransaction().commit();
        
        em.close();
        emf.close();

    }
}
