package org.scec.sha.calc;

import org.scec.sha.fault.*;

/**
 * <b>Title:</b> WC1994_MagAreaRelationship<p>
 *
 * <b>Description:</b> MagAreaRelationshipAPI implementation. Not
 * implemented yet, just the method API shell in palce.<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public class WC1994_MagAreaRelationship implements MagAreaRelationshipAPI{


   private String name = "WC1994_MagAreaRelationship";
    // ***********************
    /** @todo Iterator Class */
    // ***********************

    public WC1994_MagAreaRelationship() { }


    // ****************************
    /** @todo MagRelationship API */
    // ****************************

    public double getMeanMag(double area) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("getMeanMag() Not implemented.");
    }
    public double getMagStdev() throws UnsupportedOperationException{
        throw new UnsupportedOperationException("getMagStdev() Not implemented.");
    }

    public double getMeanArea(double mag) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("getMeanArea() Not implemented.");
    }
    public double getAreaStdev()throws UnsupportedOperationException{
        throw new UnsupportedOperationException("getAreaStdev() Not implemented.");
    }

    /**
    * Returns the name of the class
    *
    * @return String specifying the class name
    */
   public String getName() {
     return name;
    }
}
