package org.scec.sha.calc;

//import org.scec.sha.fault.GaussianStatistics;

/**
 * <b>Title:</b> MagAreaRelationshipAPI<br>
 * <b>Description:</b> This is an interface that takes a rupture area (or magnitude)
 * and returns the mean and standard deviation magnitude (or area, in
 * km-squared).  Implementing classes will instantiate specific models such as
 * Wells and Coppersmith (1994).  Subclasses may also have faulting
 * type parameters within.  Any subclass can be passed to any object
 * that takes this abstract class as input.<br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public interface MagAreaRelationshipAPI{

    public double getMeanArea(double mag);
    public double getAreaStdev(double mag);

    public double getMeanMag(double area);
    public double getMagStdev(double area);

}
