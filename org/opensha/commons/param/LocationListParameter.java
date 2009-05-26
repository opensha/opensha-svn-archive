package org.opensha.commons.param;

import java.util.ListIterator;

import org.dom4j.Element;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.param.editor.LocationParameterEditor;
import org.opensha.commons.param.editor.ParameterEditor;

/**
 * <p>Title: LocationListParameter</p>
 * <p>Description: Make a parameter which is basically a parameterList for location
 * parameters.</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @created : April 01,2004
 * @version 1.0
 */

public class LocationListParameter extends DependentParameter
implements java.io.Serializable{


	/** Class name for debugging. */
	protected final static String C = "LocationListParameter";
	/** If true print out debug statements. */
	protected final static boolean D = false;

	protected final static String PARAM_TYPE ="LocationListParameter";



	private final static String LOCATION_PARAM_NAME = "Location - ";
	private final static String LAT_PARAM_NAME = "Latitude";
	private final static String LON_PARAM_NAME = "Longitude";
	private final static String DEPTH_PARAM_NAME = "Depth";

	private final static String LAT_PARAM_UNITS = "degrees";
	private final static String LON_PARAM_UNITS = "degrees";
	private final static String DEPTH_PARAM_UNITS = "Kms";

	private ParameterEditor paramEdit = null;
	
	/**
	 *  No constraints specified for this parameter. Sets the name of this
	 *  parameter.
	 *
	 * @param  name  Name of the parameter
	 */
	public LocationListParameter(String name) {
		super(name,null,null,null);
	}


	/**
	 * No constraints specified, all values allowed. Sets the name and value.
	 *
	 * @param  name   Name of the parameter
	 * @param  locList  LocationList  object
	 */
	public LocationListParameter(String name, LocationList locList){
		super(name,null,null,locList);

	}



	/**
	 *  Compares the values to if this is less than, equal to, or greater than
	 *  the comparing objects.
	 *
	 * @param  obj                     The object to compare this to
	 * @return                         -1 if this value < obj value, 0 if equal,
	 *      +1 if this value > obj value
	 * @exception  ClassCastException  Is thrown if the comparing object is not
	 *      a ParameterListParameter.
	 */
	public int compareTo( Object obj ) {
		String S = C + ":compareTo(): ";

		if ( !( obj instanceof LocationListParameter ) ) {
			throw new ClassCastException( S + "Object not a LocationListParameter, unable to compare" );
		}

		LocationListParameter param = ( LocationListParameter ) obj;

		if( ( this.value == null ) && ( param.value == null ) ) return 0;
		int result = 0;

		LocationList n1 = ( LocationList) this.getValue();
		LocationList n2 = ( LocationList ) param.getValue();

		return n1.compareTo( n2 );
	}


	/**
	 *
	 * @param locationParameters ParameterList
	 */
	public void setAllLocations(ParameterList locationParameters){

		//setting the independent Param List for this parameter
		setIndependentParameters(locationParameters);
		LocationList locList = new LocationList();
		ListIterator it = locationParameters.getParametersIterator();
		while(it.hasNext()){
			LocationParameter locParam = (LocationParameter)it.next();
			locList.addLocation((Location)locParam.getValue());
		}
		setValue(locList);
	}


	/**
	 * Set's the parameter's value, which is basically a parameterList.
	 *
	 * @param  value                 The new value for this Parameter
	 * @throws  ParameterException   Thrown if the object is currenlty not
	 *      editable
	 */
	public void setValue( LocationList value ) throws ParameterException {

		setValue( (Object) value );

	}

	/**
	 * Compares value to see if equal.
	 *
	 * @param  obj                     The object to compare this to
	 * @return                         True if the values are identical
	 * @exception  ClassCastException  Is thrown if the comparing object is not
	 *      a LocationListParameter.
	 */
	public boolean equals(Object obj) {
		String S = C + ":equals(): ";

		if (! (obj instanceof LocationListParameter)) {
			throw new ClassCastException(S +
					"Object not a LocationListParameter, unable to compare");
		}

		String otherName = ( (LocationListParameter) obj).getName();
		if ( (compareTo(obj) == 0) && getName().equals(otherName)) {
			return true;
		}
		else {
			return false;
		}
	}

	/*  This function just checks that we only allow an object of LocationList.
	 *
	 * @param  obj  Object to check if allowed via constraints
	 * @return      True if the value is allowed
	 */
	public boolean isAllowed(Object obj) {
		if(obj == null && this.isNullAllowed()) return true;
		if(obj instanceof LocationList) return true;
		else return false;
	}

	/**
	 *  Returns a copy so you can't edit or damage the origial.
	 *
	 * @return    Exact copy of this object's state
	 */
	public Object clone(){

		LocationListParameter param = null;
		if( value == null ) param = new LocationListParameter( name);
		else param = new LocationListParameter(name,(LocationList)value);
		if( param == null ) return null;
		param.editable = true;
		param.info = info;
		return param;
	}


	/**
	 *
	 * @returns the locationList contained in this parameter
	 */
	public LocationList getParameter(){
		return (LocationList)getValue();
	}

	/**
	 * Returns the name of the parameter class
	 */
	public String getType() {
		String type = this.PARAM_TYPE;
		return type;
	}

	/**
	 * This overrides the getmetadataString() method because the value here
	 * does not have an ASCII representation (and we need to know the values
	 * of the independent parameter instead).
	 * @returns Sstring
	 */
	public String getMetadataString() {
		return getDependentParamMetadataString();
	}


	public boolean setValueFromXMLMetadata(Element el) {
		// TODO Auto-generated method stub
		return false;
	}

	public ParameterEditor getEditor() {
		if (paramEdit == null) {
			paramEdit = new LocationParameterEditor(this);
		}
		return paramEdit;
	}


}


