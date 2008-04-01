package org.opensha.data.function;

import java.util.Iterator;

import org.dom4j.Element;
import org.opensha.exceptions.*;
import org.opensha.data.NamedObjectAPI;


/**
 * <b>Title:</b> DiscretizedFunc<p>
 *
 * <b>Description:</b> Abstract implementation of the DiscretizedFuncAPI. Performs standard
 * simple or default functions so that subclasses don't have to keep reimplementing the
 * same function bodies.<p>
 *
 * A Discretized Function is a collection of x and y values grouped together as
 * the points that describe a function. A discretized form of a function is the
 * only ways computers can represent functions. Instead of having y=x^2, you
 * would have a sample of possible x and y values. <p>
 *
 * The basic functions this abstract class implements are:<br>
 * <ul>
 * <li>get, set Name()
 * <li>get, set, Info()
 * <li>get, set, Tolerance()
 * <li>equals() - returns true if all three fields have the same values.
 * </ul>
 *
 * See the interface documentation for further explanation of this framework<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public abstract class DiscretizedFunc implements DiscretizedFuncAPI,
    NamedObjectAPI,java.io.Serializable{


    /** Class name used for debbuging */
    protected final static String C = "DiscretizedFunc";
    /** if true print out debugging statements */
    protected final static boolean D = false;
    
    public final static String XML_METADATA_NAME = "discretizedFunction";
    public final static String XML_METADATA_POINTS_NAME = "Points";
    public final static String XML_METADATA_POINT_NAME = "Point";


    /**
     * The tolerance allowed in specifying a x-value near a real x-value,
     * so that the real x-value is used. Note that the tolerance must be smaller
     * than 1/2 the delta between data points for evenly discretized function, no
     * restriction for arb discretized function, no standard delta.
     */
    protected double tolerance = 0.0;

    /**
     * Information about this function, will be used in making the legend from
     * a parameter list of variables
     */
    protected String info = "";

    /**
     * Name of the function, useful for differentiation different instances
     * of a function, such as in an array of functions.
     */
    protected String name = "";

    //X and Y Axis name
    private String xAxisName,yAxisName;

    /** Returns the name of this function. */
     public String getName(){ return name; }
    /** Sets the name of this function. */
    public void setName(String name){ this.name = name; }


    /** Returns the info of this function. */
    public String getInfo(){ return info; }
    /** Sets the info string of this function. */
     public void setInfo(String info){ this.info = info; }


    /**Returns the tolerance of this function. */
    public double getTolerance() { return tolerance; }
    /**
     * Sets the tolerance of this function. Throws an InvalidRangeException
     * if the tolerance is less than zero, an illegal value.
     */
     public void setTolerance(double newTolerance) throws InvalidRangeException {
        if( newTolerance < 0 )
            throw new InvalidRangeException("Tolerance must be larger or equal to 0");
        tolerance = newTolerance;
    }


    /**
     * Sets the name of the X Axis
     * @param xName String
     */
    public void setXAxisName(String xName){
      xAxisName = xName;
    }

    /**
     * Gets the name of the X Axis
     * @return String
     */
    public String getXAxisName(){
      return xAxisName;
    }

    /**
     * Sets the name of the Y Axis
     * @param yName String
     */
    public void setYAxisName(String yName){
      yAxisName = yName;
    }

    /**
     * Gets the name of the Y Axis
     * @return String
     */
    public String getYAxisName(){
      return yAxisName;
    }



    /**
     * Default equals for all Discretized Functions. Determines if two functions
     * are the same by comparing that the name and info are the same. Can
     * be overridden by subclasses for different requirements
     */
    public boolean equals(DiscretizedFuncAPI function){
        if( !getName().equals(function.getName() )  ) return false;

        if( D ) {
            String S = C + ": equals(): ";
            System.out.println(S + "This info = " + getInfo() );
            System.out.println(S + "New info = " + function.getInfo() );

        }

        if( !getInfo().equals(function.getInfo() )  ) return false;
        return true;
    }
    
    public Element toXMLMetadata(Element root) {
    	Element xml = root.addElement(DiscretizedFunc.XML_METADATA_NAME);
    	
    	xml.addAttribute("info", this.getInfo());
    	xml.addAttribute("name", this.getName());
    	
    	xml.addAttribute("tolerance", this.getTolerance() + "");
    	xml.addAttribute("xAxisName", this.getXAxisName());
    	xml.addAttribute("yAxisName", this.getYAxisName());
    	xml.addAttribute("num", this.getNum() + "");
    	
    	Element points = xml.addElement(DiscretizedFunc.XML_METADATA_POINTS_NAME);
    	for (int i=0; i<this.getNum(); i++) {
    		Element point = points.addElement(DiscretizedFunc.XML_METADATA_POINT_NAME);
    		point.addAttribute("x", this.getX(i) + "");
    		point.addAttribute("y", this.getY(i) + "");
    	}
    	
    	return root;
    }
    
    public static ArbitrarilyDiscretizedFunc fromXMLMetadata(Element funcElem) {
    	ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
    	
    	String info = funcElem.attributeValue("info");
    	String name = funcElem.attributeValue("name");
    	String xAxisName = funcElem.attributeValue("xAxisName");
    	String yAxisName = funcElem.attributeValue("yAxisName");
    	
    	double tolerance = Double.parseDouble(funcElem.attributeValue("tolerance"));
    	
    	func.setInfo(info);
    	func.setName(name);
    	func.setXAxisName(xAxisName);
    	func.setYAxisName(yAxisName);
    	func.setTolerance(tolerance);
    	
    	Element points = funcElem.element(DiscretizedFunc.XML_METADATA_POINTS_NAME);
    	Iterator<Element> it = points.elementIterator();
    	while (it.hasNext()) {
    		Element point = it.next();
    		double x = Double.parseDouble(point.attributeValue("x"));
    		double y = Double.parseDouble(point.attributeValue("y"));
    		func.set(x, y);
    	}
    	
		return func;
    }

}
