package org.scec.sha.earthquake;

import java.util.*;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.surface.*;
import org.scec.util.*;

/**
 *
 * <b>Title:</b> ProbEqkRupture<br>
 * <b>Description:</b> <br>
 * <b>Revision History:</b><BR>
 * <ul>
 *  <li>1/2/2002
 *      <ul>
 *      <li>SWR: Removed extends ParameterList
 *      </ul>
 * </ul>
 * <br>
 *
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Sid Hellman
 * @version 1.0
 */

public class ProbEqkRupture {

    /* *******************/
    /** @todo  Variables */
    /* *******************/

    /* Debbuging variables */
    protected final static String C = "ProbEqkRupture";
    protected final static boolean D = false;

	protected double mag;
	protected double aveRake;
	protected double probability;
    protected Location hypocenterLocation = null;

    /** Represents a start time and duration => has end time */
    protected TimeSpan timespan = null;

    /** object to specify Rupture distribution and AveDip */
    protected GriddedSurfaceAPI ruptureSurface = null;

    /** object to contain arbitrary parameters */
    protected ParameterList peParameters = new ParameterList();




    /* **********************/
    /** @todo  Constructors */
    /* **********************/

    public ProbEqkRupture() {


    }

    public ProbEqkRupture(
        double mag,
        double aveRake,
        double probability,
	    GriddedSurfaceAPI ruptureSurface,
		Location hypocenterLocation) throws InvalidRangeException{

        FaultUtils.assertValidRake(aveRake);

    }



    /* ***************************/
    /** @todo  Getters / Setters */
    /* ***************************/

    /**
     * This function doesn't create the ParameterList until the
     * first attempt to add a parameter is added. This is known as
     * Lazy Instantiation, where the class is not created until needed.
     * This is a common performance enhancement, because in general, not all
     * aspects of a program are used per user session.
     */
    public void addParameter(ParameterAPI parameter){
        if( peParameters == null) peParameters = new ParameterList();
        if(!peParameters.containsParameter(parameter)){
            peParameters.addParameter(parameter);
        }
        else{ peParameters.updateParameter(parameter); }
    }

    public void removeParameter(ParameterAPI parameter){
        if( peParameters == null) return;
        peParameters.removeParameter(parameter);
    }

    /**
     * SWR - Not crazy about the name, why not just getParametersIterator(),
     * same as the ParameterList it is calling. People don't know that they
     * have been Added, this doesn't convey any more information than the
     * short name to me.
     */
    public ListIterator getAddedParametersIterator(){
        if( peParameters == null) return null;
        else{ return peParameters.getParametersIterator(); }
    }

	public double getMag() { return mag; }
	public void setMag(double mag) { this.mag = mag; }

	public double getAveRake() { return aveRake; }
	public void setAveRake(double aveRake) throws InvalidRangeException{
        FaultUtils.assertValidRake(aveRake);
        this.aveRake = aveRake;
    }

	public double getProbability() { return probability; }
	public void setProbability(double p) { probability = p; }

	public GriddedSurfaceAPI getRuptureSurface() { return ruptureSurface; }
    /**
     * Note: Since this takes a GriddedSurfaceAPI both a
     * PointSurface and GriddedSurface can be set here
     */
	public void setRuptureSurface(GriddedSurfaceAPI r) { ruptureSurface = r; }

	public Location getHypocenterLocation() { return hypocenterLocation; }
	public void setHypocenterLocation(Location h) { hypocenterLocation = h; }

    public TimeSpan getTimeSpan() { return timespan; }
	public void setTimeSpan(TimeSpan timespan) { this.timespan = timespan; }

    /**
     * This is a function of probability and timespan
     */
    public double getMeanAnnualRate(){
        return 0;
    }

    public void setPointSurface(Location location){}
    public void setPointSurface(Location location, double aveStrike, double aveDip){}
    public void setPointSurface(PointSurface pointSurface){}



}
