package org.scec.data;

import org.scec.param.*;

// FIX - Needs more comments

/**
 *  <b>Title:</b> Direction<p>
 *  <b>Description:</b> Basic container class that represents a distance vector
 *  between two Location objects<p>
 *
 * This class contains the fields vertical distance, horizontal distance,
 * azimuth and back azimuth. Thses fields describe any point on or under the
 * surface of the earth with respect to another point. <p>
 *
 *
 * @author     Sid Hellman
 * @created    February 26, 2002
 * @version    1.0
 */

public class Direction {

    /** Class name used for debbuging */
    protected final static String C = "Location";
    /** if true print out debugging statements */
    protected final static boolean D = false;


    /**
     *  Description of the Field
     */
    protected double vertDistance;
    /**
     *  Description of the Field
     */
    protected double horzDistance;
    /**
     *  Description of the Field
     */
    protected DoubleParameter azimuth = new DoubleParameter("azimuth");
    /**
     *  Description of the Field
     */
    protected DoubleParameter backAzimuth = new DoubleParameter("backAzimuth");


    /**
     *  Constructor for the Direction object
     */
    public Direction() {

        this.azimuth.setValue( new Double(180) );
        this.backAzimuth.setValue( new Double(0) );
        this.horzDistance = 0;
        this.vertDistance = 0;

    }


    /**
     *  Constructor for the Direction object
     *
     * @param  vDist   Description of the Parameter
     * @param  hDist   Description of the Parameter
     * @param  az      Description of the Parameter
     * @param  backAz  Description of the Parameter
     */
    public Direction( double vDist, double hDist, double az, double backAz ) {

        this.azimuth.setValue( new Double(az) );
        this.backAzimuth.setValue( new Double(backAz) );
        this.horzDistance = hDist;
        this.vertDistance = vDist;
    }


    /**
     *  Sets the vertDistance attribute of the Direction object
     *
     * @param  vertDistance  The new vertDistance value
     */
    public void setVertDistance( double vertDistance ) {
        this.vertDistance = vertDistance;
    }


    /**
     *  Sets the horzDistance attribute of the Direction object
     *
     * @param  horzDistance  The new horzDistance value
     */
    public void setHorzDistance( double horzDistance ) {
        this.horzDistance = horzDistance;
    }


    /**
     *  Sets the azimuth attribute of the Direction object
     *
     * @param  azimuth        The new azimuth value
     * @exception  Exception  Description of the Exception
     */
    public void setAzimuth( double azimuth ) {
        this.azimuth.setValue( new Double( azimuth ) );
    }


    /**
     *  Sets the backAzimuth attribute of the Direction object
     *
     * @param  backAzimuth    The new backAzimuth value
     * @exception  Exception  Description of the Exception
     */
    public void setBackAzimuth( double backAzimuth ) {
        this.backAzimuth.setValue( new Double( backAzimuth ) );
    }


    /**
     *  Gets the vertDistance attribute of the Direction object
     *
     * @return    The vertDistance value
     */
    public double getVertDistance() {
        return vertDistance;
    }


    /**
     *  Gets the horzDistance attribute of the Direction object
     *
     * @return    The horzDistance value
     */
    public double getHorzDistance() {
        return horzDistance;
    }


    /**
     *  Gets the azimuth attribute of the Direction object
     *
     * @return    The azimuth value
     */
    public double getAzimuth() {
        return ( ( Double ) azimuth.getValue() ).doubleValue();
    }


    /**
     *  Gets the backAzimuth attribute of the Direction object
     *
     * @return    The backAzimuth value
     */
    public double getBackAzimuth() {
        return ( ( Double ) backAzimuth.getValue() ).doubleValue();
    }



    public String toString(){

        StringBuffer b = new StringBuffer();
        b.append(C);
        //b.append('\n');
        b.append(" : ");

        b.append("horzDistance = ");
        b.append(horzDistance);
        //b.append('\n');
        b.append(" : ");


        b.append("vertDistance = ");
        b.append(vertDistance);
        //b.append('\n');
        b.append(" : ");


        b.append("azimuth = ");
        b.append(azimuth);
        //b.append('\n');
        b.append(" : ");

        b.append("backAzimuth = ");
        b.append(backAzimuth);
        //b.append('\n');
        b.append(" : ");

        return b.toString();

    }

    public boolean equalsDirection(Direction dir){

        if(this.horzDistance != dir.horzDistance ) return false;
        if(this.vertDistance != dir.vertDistance ) return false;
        if(this.azimuth != dir.azimuth ) return false;
        if(this.backAzimuth != dir.backAzimuth ) return false;

        return true;
    }

    public boolean equals(Object obj){
        if(obj instanceof Direction) return equalsDirection( (Direction)obj );
        else return false;
    }
}
