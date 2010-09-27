/*
 * FindPlayers.java
 *
 * Copyright 2007 Sun Microsystems, Inc. ALL RIGHTS RESERVED Use of 
 * this software is authorized pursuant to the terms of the license 
 * found at http://developers.sun.com/berkeley_license.html.
 *
 *
 */

package com.ubs.punter.jpa;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author John O'Conner
 */
public class RemovePlayer {
    
    /** Creates a new instance of FindPlayers */
    public RemovePlayer() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Create the EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("league");
        EntityManager em = emf.createEntityManager();
        
        em.getTransaction().begin();
        Player player = em.find(Player.class, 5L);
        if (player != null) {
            System.out.println(player.toString());
            em.remove(player);
        }
        
        em.getTransaction().commit();
        
        em.close();
        emf.close();
    }
    
    
}
