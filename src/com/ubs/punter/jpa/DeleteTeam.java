/*
 * DeleteTeam.java
 *
 * Copyright 2007 Sun Microsystems, Inc. ALL RIGHTS RESERVED Use of 
 * this software is authorized pursuant to the terms of the license 
 * found at http://developers.sun.com/berkeley_license.html.
 *
 *
 */

package com.ubs.punter.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author John O'Conner
 */
public class DeleteTeam {
    
    /** Creates a new instance of DeleteTeam */
    private DeleteTeam() {
    }
    
    public static void main(String[] args) {
        String aTeamName = "Anaheim Angels";
        
        // Create the EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("league");
        EntityManager em = emf.createEntityManager();
        
        Query q = em.createQuery("delete from Team t " +
                "where t.teamName = :name");
        q.setParameter("name", aTeamName);
        
        em.getTransaction().begin();
        q.executeUpdate();
        em.getTransaction().commit();
        
        em.close();
        emf.close();
        
    }
    
}
