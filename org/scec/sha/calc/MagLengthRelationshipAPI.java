package org.scec.sha.calc;

//import org.scec.sha.fault.GaussianStatistics;

/**
 * <b>Title:</b> MagLengthRelationshipAPI<br>
 * <b>Description:</b> This is an interface that takes a rupture length (or
 * magnitude) and returns the mean and standard deviation magnitude
 * (or length).  Implementing classes will instantiate specific models such as
 * Wells and Coppersmith (1994).  Subclasses may also depend on
 * faulting type.<br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Sid Hellman
 * @version 1.0
 */

public interface MagLengthRelationshipAPI{

    public  double getMeanLength(double mag);
    public  double getLengthStdev(double mag);

    public double getMeanMag(double length);
    public double getMagStdev(double length);
}
