package org.opensha.sha.earthquake;

import java.util.*;

import org.opensha.data.*;
import org.opensha.exceptions.*;
import org.opensha.param.*;
import org.opensha.sha.surface.*;
import org.opensha.util.*;

/**
 *
 * <b>Title:</b> EqkRupture<br>
 * <b>Description:</b> <br>
 *
 * @author Sid Hellman
 * @version 1.0
 */

public class EqkRupture implements java.io.Serializable {

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
    protected ParameterList otherParams ;




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
      this.mag = mag;
      FaultUtils.assertValidRake(aveRake);
      this.hypocenterLocation = hypocenterLocation;
      this.aveRake = aveRake;
      this.ruptureSurface = ruptureSurface;
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

    public String getInfo() {
      String info1, info2;
      info1 = new String("\tMag. = " + (float) mag + "\n" +
                         "\tAve. Rake = " + (float) aveRake + "\n" +
                         "\tAve. Dip = " + (float) ruptureSurface.getAveDip() +
                         "\n" +
                         "\tHypocenter = " + hypocenterLocation + "\n");

      // write our rupture surface information
      if (ruptureSurface.getNumCols() == 1 && ruptureSurface.getNumRows() == 1) {
        Location loc = ruptureSurface.getLocation(0, 0);
        info2 = new String("\tPoint-Surface Location (lat, lon, depth (km):" +
                           "\n\n" +
                           "\t\t" + (float) loc.getLatitude() + ", " +
                           (float) loc.getLongitude() +
                           ", " + (float) loc.getDepth());
      }
      else {
        Location loc1 = ruptureSurface.getLocation(0, 0);
        Location loc2 = ruptureSurface.getLocation(0,
                                                   ruptureSurface.getNumCols() - 1);
        Location loc3 = ruptureSurface.getLocation(ruptureSurface.getNumRows() -
                                                   1, 0);
        Location loc4 = ruptureSurface.getLocation(ruptureSurface.getNumRows() -
                                                   1,
                                                   ruptureSurface.getNumCols() - 1);
        info2 = new String("\tRup. Surf. Corner Locations (lat, lon, depth (km):" +
                           "\n\n" +
                           "\t\t" + (float) loc1.getLatitude() + ", " +
                           (float) loc1.getLongitude() + ", " +
                           (float) loc1.getDepth() + "\n" +
                           "\t\t" + (float) loc2.getLatitude() + ", " +
                           (float) loc2.getLongitude() + ", " +
                           (float) loc2.getDepth() + "\n" +
                           "\t\t" + (float) loc3.getLatitude() + ", " +
                           (float) loc3.getLongitude() + ", " +
                           (float) loc3.getDepth() + "\n" +
                           "\t\t" + (float) loc4.getLatitude() + ", " +
                           (float) loc4.getLongitude() + ", " +
                           (float) loc4.getDepth() + "\n");
      }
      return info1 + info2;
    }


    /**
     * Creates the XML representation for the Eqk Rupture Object
     * @return String
     */
    public String ruptureXML_String(){

      String rupInfo = "<EqkRupture>\n";
      rupInfo +="<Mag>"+mag+"</Mag>\n";
      rupInfo +="<Ave.Rake>"+aveRake+"</Ave.Rake>\n";
      rupInfo +="<Ave.Dip>"+ruptureSurface.getAveDip()+"</Ave.Dip>\n";
      rupInfo +="<NumRows>"+(ruptureSurface.getNumRows()-1)+"</NumRows>\n";
      rupInfo +="<NumCols>"+(ruptureSurface.getNumCols()-1)+"</NumCols>\n";
      rupInfo +="<EqkRupLatGridSpacing>"+ruptureSurface.getGridSpacingForLat()+"</EqkRupLatGridSpacing>\n";
      rupInfo +="<EqkRupLonGridSpacing>"+ruptureSurface.getGridSpacingForLon()+"</EqkRupLonGridSpacing>\n";
      rupInfo +="<EqkRupture-Params>\n";
      if(otherParams !=null){
        ListIterator it = otherParams.getParametersIterator();
        while(it.hasNext()){
          ParameterAPI param = (ParameterAPI) it.next();
          rupInfo += param.getName() +"="+ param.getValue() + "\n";
        }
      }
      rupInfo +="</EqkRupture-Params>\n";
      rupInfo +="<RuptureGridCenteredSurfaceLocations>\n";
      GriddedSurfaceAPI rupSurface= ruptureSurface.getGridCenteredSurface();
      ListIterator it = rupSurface.getLocationsIterator();
      while(it.hasNext()){
        Location loc = (Location)it.next();
        rupInfo +=loc.getLatitude()+","+loc.getLongitude()+","+loc.getDepth()+
            ","+ruptureSurface.getAveStrike()+"\n";
      }
      rupInfo +="</RuptureGridCenteredSurfaceLocations>\n";
      rupInfo +="</EqkRupture>\n";
      return rupInfo;
    }

}
