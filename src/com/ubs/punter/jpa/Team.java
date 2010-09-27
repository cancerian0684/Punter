/*
 * Team.java
 *
 * Copyright 2007 Sun Microsystems, Inc. ALL RIGHTS RESERVED Use of 
 * this software is authorized pursuant to the terms of the license 
 * found at http://developers.sun.com/berkeley_license.html.
 *
 */

package com.ubs.punter.jpa;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * Entity class Team
 * 
 * @author John O'Conner
 */
@Entity
public class Team implements Serializable {

    private Long id;
    private String teamName;
    private String league;
    private Collection<Player> players;
    
    /** Creates a new instance of Team */
    public Team() {
        players = new HashSet();
    }

    public Team(String name, String league) {
        this.teamName = name;
        this.league = league;
        players = new HashSet();
    }
    /**
     * Gets the id of this Team.
     * @return the id
     */
    @Id
    @GeneratedValue
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the id of this Team to the specified value.
     * @param id the new id
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTeamName() {
        return teamName;
    }
    
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
    
    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        this.league = league;
    }
    
    @OneToMany(mappedBy = "team")
    public Collection<Player> getPlayers() {
        return players;
    }
    
    public void setPlayers(Collection<Player> players) {
        this.players = players;
    }
    
    public void addPlayer(Player player) {
        players.add(player);
    }
    
    public boolean removePlayer(Player player) {
        return players.remove(player);
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
     * Determines whether another object is equal to this Team.  The result is 
     * <code>true</code> if and only if the argument is not null and is a Team object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Team)) {
            return false;
        }
        Team other = (Team)object;
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
        return "com.sun.demo.jpa.Team[id=" + id + "]";
    }
    
}
