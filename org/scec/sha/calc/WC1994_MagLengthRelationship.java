package org.scec.sha.calc;

import org.scec.sha.fault.*;

/**
 * <b>Title:</b>WC1994_MagLengthRelationship<br>
 * <b>Description:</b> <br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class WC1994_MagLengthRelationship implements MagLengthRelationshipAPI{

    // Set default rake as strike slip (0 degrees)
    private double rake = 0;

    public WC1994_MagLengthRelationship() {



    }


    // ****************************
    /** @todo MagRelationship API */
    // ****************************

    public double getMeanMag(double area) throws UnsupportedOperationException{

        throw new UnsupportedOperationException("hasPrevious() Not implemented.");
    }
    public double getMagStdev(double area) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("hasPrevious() Not implemented.");
    }


    /**
     * This method assumes strike slip rupture (rake withing 45 degrees of
     * 0 or 180)
     * @param mag
     * @return
     */
    public double getMeanLength(double mag)  {
        return -3.55+0.74*mag;
    }
    public double getLengthStdev(double mag)throws UnsupportedOperationException{
        throw new UnsupportedOperationException("hasPrevious() Not implemented.");
    }
}
