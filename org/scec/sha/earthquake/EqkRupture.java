package org.scec.sha.earthquake;

import java.util.*;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.surface.*;
import org.scec.util.*;

/**
 *
 * <b>Title:</b> EqkRupture<br>
 * <b>Description:</b> <br>
 *
 * @author Sid Hellman
 * @version 1.0
 */

public class EqkRupture {

    /* *******************/
    /** @todo  Variables */
    /* *******************/

    /* Debbuging variables */
    protected final static String C = "EqkRupture";
    protected final static boolean D = false;

    protected double mag=Double.NaN;
    protected double aveRake=Double.NaN;

    protected Location hypocenterLocation = null;



    /** object to specify Rupture distribution and AveDip */
    protected GriddedSurfaceAPI ruptureSurface = null;

    /** object to contain arbitrary parameters */
    protected ParameterList otherParams = new ParameterList();




    /* **********************/
    /** @todo  Constructors */
    /* **********************/

    public EqkRupture() {


    }

    public EqkRupture(
        double mag,
        double aveRake,
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
        if( otherParams == null) otherParams = new ParameterList();
        if(!otherParams.containsParameter(parameter)){
            otherParams.addParameter(parameter);
        }
        else{ otherParams.updateParameter(parameter); }
    }

    public void removeParameter(ParameterAPI parameter){
        if( otherParams == null) return;
        otherParams.removeParameter(parameter);
    }

    /**
     * SWR - Not crazy about the name, why not just getParametersIterator(),
     * same as the ParameterList it is calling. People don't know that they
     * have been Added, this doesn't convey any more information than the
     * short name to me.
     */
    public ListIterator getAddedParametersIterator(){
        if( otherParams == null) return null;
        else{ return otherParams.getParametersIterator(); }
    }

    public double getMag() { return mag; }
    public void setMag(double mag) { this.mag = mag; }

    public double getAveRake() { return aveRake; }
    public void setAveRake(double aveRake) throws InvalidRangeException{
        FaultUtils.assertValidRake(aveRake);
        this.aveRake = aveRake;
    }


    public GriddedSurfaceAPI getRuptureSurface() { return ruptureSurface; }


    /**
     * Note: Since this takes a GriddedSurfaceAPI both a
     * PointSurface and GriddedSurface can be set here
     */
    public void setRuptureSurface(GriddedSurfaceAPI r) { ruptureSurface = r; }

    public Location getHypocenterLocation() { return hypocenterLocation; }
    public void setHypocenterLocation(Location h) { hypocenterLocation = h; }



    public void setPointSurface(Location location){
        PointSurface ps = new PointSurface(location.getLatitude(), location.getLongitude(), location.getDepth());
        setPointSurface(ps);
    }

    public void setPointSurface(Location location, double aveDip ){
        setPointSurface(location);
        ruptureSurface.setAveDip(aveDip);
    }

    public void setPointSurface(Location location, double aveStrike, double aveDip){
        setPointSurface(location);
        ruptureSurface.setAveStrike(aveStrike);
        ruptureSurface.setAveDip(aveDip);
    }

    public void setPointSurface(PointSurface pointSurface){
        this.ruptureSurface = pointSurface;
    }

}
