package org.scec.sha.earthquake;

import java.util.*;
import org.scec.data.TimeSpan;

/**
 * <b>Title:</b> ProbEqkRuptureList <br>
 * <b>Description:</b> List container for ProbEqkRuptures<br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class ProbEqkRuptureList {

    /* *******************/
    /** @todo  Variables */
    /* *******************/

    /* Debbuging variables */
    protected final static String C = "ProbEqkRuptureList";
    protected final static boolean D = false;

    protected ProbEqkRuptureInfo info;
    protected Vector potentialEarthquakes = new Vector();


    /* **********************/
    /** @todo  Constructors */
    /* **********************/

    /** no arg constructor */
    public ProbEqkRuptureList() {}


    /* ***************************/
    /** @todo  Getters / Setters */
    /* ***************************/

    /** adds the potential earthquake if it doesn't exist, else throws exception */
    public void addProbEqkRupture(ProbEqkRupture pe){

    }

    /** returns potential earthquake if exist else throws exception */
    public ProbEqkRupture getProbEqkRupture(int index){
        return null;
    }

    /** checks if the potential earthquake exists in the list */
    public boolean containsProbEqkRupture(ProbEqkRupture pe){
        return false;
    }

    /** removes potential earthquake if it exists, else
     *  throws exception
     */
    public void removeProbEqkRupture(ProbEqkRupture pe){
    }

    /**
     * updates an existing potential earthquake with the new value,
     * throws exception if potential earthquake doesn't exist
     */
    public void updateProbEqkRupture(ProbEqkRupture pe, int index){

    }

    public ListIterator listIterator(){ return potentialEarthquakes.listIterator(); }
    public void clear(){ potentialEarthquakes.clear(); }
    public int size(){ return potentialEarthquakes.size(); }

    public void setInfo(ProbEqkRuptureInfo newInfo) { info = newInfo; }
    public ProbEqkRuptureInfo getInfo() { return info; }

}
