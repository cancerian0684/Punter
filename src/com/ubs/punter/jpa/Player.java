/*
 * Player.java
 *
 *
 * Copyright 2007 Sun Microsystems, Inc. ALL RIGHTS RESERVED Use of 
 * this software is authorized pursuant to the terms of the license 
 * found at http://developers.sun.com/berkeley_license.html.
 *
 *
 */

package com.ubs.punter.jpa;

import com.ubs.punter.jpa.Team;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Entity class Player
 *
 * @author John O'Conner
 */
@Entity
public class Player implements Serializable {
    
    private Long    id;
    private String  lastName;
    private String  firstName;
    private int     jerseyNumber;
    private String  lastSpokenWords;
    private Team    team;
    
    
    
    /** Creates a new instance of Player */
    public Player() {
    }
    
    public Player(String lName, String fName, int number, String words) {
        this.lastName = lName;
        this.firstName = fName;
        this.jerseyNumber = number;
        this.lastSpokenWords = words;
    }
    
    /**
     * Gets the id of this Player. The persistence provide should
     * autogenerate a unique id for new player objects.
     * @return the id
     */
    @Id
    @GeneratedValue
    public Long getId() {
        return this.id;
    }
    
    /**
     * Sets the id of this Player to the specified value.
     * @param id the new id
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String name) {
        lastName = name;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String name) {
        firstName = name;
    }
    
    public int getJerseyNumber() {
        return jerseyNumber;
    }
    
    public void setJerseyNumber(int number) {
        jerseyNumber = number;
    }
    
    @Transient
    public String getLastSpokenWords() {
        return lastSpokenWords;
    }
    
    public void setLastSpokenWords(String lastWords) {
        lastSpokenWords = lastWords;
    }
    
    @ManyToOne(cascade=CascadeType.PERSIST)
    public Team getTeam() {
        return team;
    }
    
    public void setTeam(Team team) {
        this.team = team;
    }
    /**
     * Returns a hash code value for the object.  This implementation computes
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
    /**
     * Determines whether another object is equal to this Player.  The result is
     * <code>true</code> if and only if the argument is not null and is a Player object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Player)) {
            return false;
        }
        Player other = (Player)object;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) return false;
        return true;
    }
    
    /**
     * Returns a string representation of the object.  This implementation constructs
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        String player = String.format("[Jersey Number: %d, Name: %s %s, Team: %s]"
                , jerseyNumber, firstName, lastName, team.getTeamName());
        return player;
    }
    
}
