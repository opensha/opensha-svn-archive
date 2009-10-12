/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.commons.data;

import org.opensha.commons.param.DoubleParameter;


/**
 *  <b>Title:</b> Direction<p>
 *
 *  <b>Description:</b> Basic JavaBean class that represents a distance vector
 *  between two Location objects<p>
 *
 * This class contains the fields:
 * <ul>
 * <li>vertical distance
 * <li>horizontal distance
 * <li>azimuth
 * <li>back azimuth
 * </ul>
 *
 *TODO - this class should accept location and bearing. when queried for backAzimuth
 *it should then be calcualted, not on init. THis whole class is confused.
 *
 * Thses fields uniquely describe the vector between any two points on or within
 * the surface of the earth.<p>
 *
 * This class is what is called a javabean class in Java. A Javabean class is really
 * a data container with fields, and corresponding getXXX() and setXXX() functions
 * matching the field names.<p>
 *
 * @author     Sid Hellman, Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */

public class Direction {

    /** Class name used for debbuging */
    protected final static String C = "Direction";

    /** if true print out debugging statements */
    protected final static boolean D = false;


    /** Depth distance between two locations */
    protected double vertDistance;

    /** Mao view distance between two geographical locations on the Earth */
    protected double horzDistance;

    /** Direct angle between two locations measured with respect to (Ned, help me out here )*/
    protected DoubleParameter azimuth = new DoubleParameter("azimuth");

    /** SWR: Not clear */
    protected DoubleParameter backAzimuth = new DoubleParameter("backAzimuth");


    /**
     *  No-Arg Constructor for the Direction object sets default values:
     *
     * <ul><li>azimuth.setValue( new Double(180) );
     * <li>backAzimuth.setValue( new Double(0) );
     * <li>horzDistance = 0;
     * <li>vertDistance = 0;
     * </ul>

     */
    public Direction() {
    	// TODO fix THIS IS WRONG and NOT a good assumption to make
    	// back azimuth is dependent on location
        this.azimuth.setValue( new Double(180) );
        this.backAzimuth.setValue( new Double(0) );
        this.horzDistance = 0;
        this.vertDistance = 0;

    }


    /**
     *  Constructor that allows setting all the fields of this Direction object
     *
     * @param  vDist   vertical distance
     * @param  hDist   horizontal distance
     * @param  az      azimuth
     * @param  backAz  back azimuth
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



    /** Debug printout of all the field values */
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

    /**
     * Checks to see if another Direction object has the same field values.
     * If it does, they are considered equal.
     * @param dir
     * @return
     */
    public boolean equalsDirection(Direction dir){

        if(this.horzDistance != dir.horzDistance ) return false;
        if(this.vertDistance != dir.vertDistance ) return false;
        if(this.azimuth != dir.azimuth ) return false;
        if(this.backAzimuth != dir.backAzimuth ) return false;

        return true;
    }

    /**
     * Calls equalsDirection(Direction dir) if passed in object is a Direction,
     * else returns false. A different class could never be considered equals, like
     * comparing apples to oranges.
     * @param obj
     * @return
     */
    public boolean equals(Object obj){
        if(obj instanceof Direction) return equalsDirection( (Direction)obj );
        else return false;
    }
}
