package org.scec.sha.calc;

import org.scec.sha.fault.*;

/**
 * <b>Title:</b> WC1994_MagAreaRelationship<br>
 * <b>Description:</b> <br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class WC1994_MagAreaRelationship implements MagAreaRelationshipAPI{


    // ***********************
    /** @todo Iterator Class */
    // ***********************

    public WC1994_MagAreaRelationship() { }


    // ****************************
    /** @todo MagRelationship API */
    // ****************************

    public double getMeanMag(double area) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("hasPrevious() Not implemented.");
    }
    public double getMagStdev() throws UnsupportedOperationException{
        throw new UnsupportedOperationException("hasPrevious() Not implemented.");
    }

    public double getMeanArea(double mag) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("hasPrevious() Not implemented.");
    }
    public double getAreaStdev()throws UnsupportedOperationException{
        throw new UnsupportedOperationException("hasPrevious() Not implemented.");
    }
}
