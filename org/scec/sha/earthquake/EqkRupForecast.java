package org.scec.sha.earthquake;

import org.scec.sha.magdist.MagFreqDistAPI;
import org.scec.data.TimeSpan;
import java.util.ListIterator;

/**
 * <b>Title:</b> EqkRupForecast<br>
 * <b>Description:</b> <br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class EqkRupForecast {

    /* *******************/
    /** @todo  Variables */
    /* *******************/

    /* Debbuging variables */
    protected final static String C = "EqkRupForecast";
    protected final static boolean D = false;

    protected ProbEqkRuptureList peList = null;
    TimeSpan timespan;


    /* **********************/
    /** @todo  Constructors */
    /* **********************/

    public EqkRupForecast() { }
    public EqkRupForecast(TimeSpan timespan) { this.timespan = timespan; }



    /* ***************************/
    /** @todo  Getters / Setters */
    /* ***************************/

    public MagFreqDistAPI getMagFreqDist(){ return null;}

    public ProbEqkRuptureList getProbEqkRuptureList(){ return peList; }
    public void setProbEqkRuptureList( ProbEqkRuptureList peList){ this.peList = peList; }
    public ListIterator getProbEqkRupturesIterator(){ return peList.listIterator(); }

    public TimeSpan getTimeSpan() { return timespan; }
	public void setTimeSpan(TimeSpan timespan) { this.timespan = timespan; }

}
