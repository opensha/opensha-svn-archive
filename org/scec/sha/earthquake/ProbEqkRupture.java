package org.scec.sha.earthquake;



import org.scec.data.*;
import org.scec.exceptions.InvalidRangeException;
import org.scec.sha.surface.GriddedSurfaceAPI;


/**
 * <p>Title:ProbEqkRupture </p>
 * <p>Description: Probabilistic Earthquake Rupture</p>
 *
 * @author Nitin Gupta & Vipin Gupta
 * @date aug 27, 2002
 * @version 1.0
 */

public class ProbEqkRupture extends EqkRupture{

  protected double probability;



  /* **********************/
    /** @todo  Constructors */
    /* **********************/

  public ProbEqkRupture() {
    super();
  }

  public ProbEqkRupture(double mag,
                        double aveRake,
                        double probability,
                        GriddedSurfaceAPI ruptureSurface,
                        Location hypocenterLocation)
                            throws InvalidRangeException {

        super(mag, aveRake, ruptureSurface, hypocenterLocation);
        this.probability = probability;

   }



  public double getProbability() { return probability; }

  public void setProbability(double p) { probability = p; }

  /**
   * This is a function of probability and timespan
   */
  public double getMeanAnnualRate(){
       return 0;
   }


   public String getInfo() {
     String info1, info2;
     info1 = new String("\tMag. = "+(float)mag+"\n"+
                        "\tAve. Rake = "+(float)aveRake+"\n"+
                        "\tProb. = "+(float)probability+"\n"+
                        "\tAve. Dip = "+(float)ruptureSurface.getAveDip()+"\n");

     // write our rupture surface information
     if(ruptureSurface.getNumCols() == 1 && ruptureSurface.getNumRows() == 1) {
       Location loc = ruptureSurface.getLocation(0,0);
       info2 = new String("\tPoint-Surface Location (lat, lon, depth (km):"+"\n\n"+
                          "\t\t"+ (float)loc.getLatitude()+", "+(float)loc.getLongitude()+
                          ", "+(float)loc.getDepth());
     }
     else {
       Location loc1 = ruptureSurface.getLocation(0,0);
       Location loc2 = ruptureSurface.getLocation(0,ruptureSurface.getNumCols()-1);
       Location loc3 = ruptureSurface.getLocation(ruptureSurface.getNumRows()-1,0);
       Location loc4 = ruptureSurface.getLocation(ruptureSurface.getNumRows()-1,ruptureSurface.getNumCols()-1);
       info2 = new String("\tRup. Surf. Corner Locations (lat, lon, depth (km):"+"\n\n"+
                          "\t\t"+ (float)loc1.getLatitude()+", "+(float)loc1.getLongitude()+", "+(float)loc1.getDepth()+"\n"+
                          "\t\t"+ (float)loc2.getLatitude()+", "+(float)loc2.getLongitude()+", "+(float)loc2.getDepth()+"\n"+
                          "\t\t"+ (float)loc3.getLatitude()+", "+(float)loc3.getLongitude()+", "+(float)loc3.getDepth()+"\n"+
                          "\t\t"+ (float)loc4.getLatitude()+", "+(float)loc4.getLongitude()+", "+(float)loc4.getDepth()+"\n");
     }
     return info1 + info2;
   }


   /**
    * Clones the eqk rupture and returns the new cloned object
    * @return
    */
  public Object clone() {
    ProbEqkRupture eqkRuptureClone=new ProbEqkRupture();
    eqkRuptureClone.setAveRake(this.aveRake);
    eqkRuptureClone.setMag(this.mag);
    eqkRuptureClone.setRuptureSurface(this.ruptureSurface);
    eqkRuptureClone.setHypocenterLocation(this.hypocenterLocation);
    eqkRuptureClone.setProbability(this.probability);
    return eqkRuptureClone;
  }

}