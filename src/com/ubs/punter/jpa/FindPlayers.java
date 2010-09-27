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
public class FindPlayers {
    
    /** Creates a new instance of FindPlayers */
    public FindPlayers() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Create the EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("league");
        EntityManager em = emf.createEntityManager();
        
        
        for(long primaryKey = 1; primaryKey < 10; primaryKey++) {
            Player player = em.find(Player.class, primaryKey);
            if (player != null) {
                System.out.println(player.toString());
            }
            
        }
        
        
        em.close();
        emf.close();
        // TODO code application logic here
    }
    
    
}
