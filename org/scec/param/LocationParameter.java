package org.scec.param;

import java.util.*;
import org.scec.exceptions.*;
import org.scec.data.Location;

/**
 * <p>Title: LocationParameter</p>
 * <p>Description: Make a Location Parameter</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @created : Aug 18, 2003
 * @version 1.0
 */

public class LocationParameter extends DependentParameter
    implements java.io.Serializable{


  /** Class name for debugging. */
  protected final static String C = "LocationParameter";
  /** If true print out debug statements. */
  protected final static boolean D = false;

  protected final static String PARAM_TYPE ="LocationParameter";

  //parameter list parameter that holds the location parameters
  protected ParameterListParameter locationParameterListParameter;

  //location parameters
  protected ParameterAPI latParam;
  protected ParameterAPI lonParam;
  protected ParameterAPI depthParam;

  //location object, value of this parameter
  private Location location;


  //location parameterlist parameter name static declaration
  private final static String LOCATION_PARAMETER_LIST_PARAMETER_NAME = "Location(lat,lon,depth)";

  /**
   * No constraints specified for this parameter. Sets the name of this
   * parameter.
   *
   * @param  name  Name of the parameter
   */
  public LocationParameter(String name) {
    super(name,null,null,null);
  }

  /**
   * Creates a location parameter with constraint being the list of locations.
   * @param name String Name of the Location Parameter
   * @param locationList ArrayList : List of the allowed locations
   */
  public LocationParameter(String name, ArrayList locationList) throws
      ConstraintException {
    super(name, new LocationConstraint(locationList), null, locationList.get(0));
    location = (Location)locationList.get(0);
  }

  /**
   *
   * Creates a location parameter with constraint being list of locations and
   * current value of the parameter from these list of locations.
   * @param name String Name of the location parameter
   * @param locationList ArrayList : List of allowed locations
   * @param value Location : Parameter value, should be one of the allowed location
   */
  public LocationParameter(String name, ArrayList locationList, Location value) throws
      ConstraintException {
    super(name, new LocationConstraint(locationList), null, value);
    location = value;
  }

  /**
   * Creates a location parameter with constraint being list of locations and
   * current value of the parameter from these list of locations.
   * @param name String Name of the location parameter
   * @param locationList ArrayList List of allowed locations
   * @param units String Parameter units
   * @param value Location : Parameter value, should be one of the allowed locations.
   */
  public LocationParameter(String name, ArrayList locationList, String units,
                           Location value) throws ConstraintException {
    super(name, new LocationConstraint(locationList), units, value);
    location = value;
  }


  /**
   * Creates a location parameter with constraint being list of locations and
   * current value of the parameter from these list of locations.
   * @param name String Parameter Name
   * @param locationListConstraint LocationConstraint: Constraint on the location parameter
   * @param units String Parameter units
   * @param value Location Parameter value, should be one that is allowed by the constraint.
   */
  public LocationParameter(String name, LocationConstraint locationListConstraint,
                           String units, Location value) throws ConstraintException{
    super(name, locationListConstraint, units, value);
    location = value;
  }

  /**
   * Creates a location parameter with constraint being list of locations and
   * current value of the parameter from these list of locations.
   * @param name String Parameter Name
   * @param locationListConstraint LocationConstraint: Constraint on the location parameter
   */
  public LocationParameter(String name, LocationConstraint locationListConstraint) throws ConstraintException {
    super(name, locationListConstraint, null,locationListConstraint.getAllowedLocations().get(0));
    Location loc = (Location)locationListConstraint.getAllowedLocations().get(0);
    location = loc;
  }

  /**
   * Creates a location parameter with constraint being list of locations and
   * current value of the parameter from these list of locations.
   * @param name String Parameter Name
   * @param locationListConstraint LocationConstraint: Constraint on the location parameter
   * @param value Location
   */
  public LocationParameter(String name, LocationConstraint locationListConstraint,
                           Location value) throws ConstraintException{
    super(name, locationListConstraint, null, value);
    location = value;
  }


  /**
   * Creates a location parameter as parameterlist parameter. This creates
   * a location parameter that holds lat param,lon param and depth parameter
   * in a parameterListParameter.
   *
   * @param locationParamName String Parameter Name
   * @param latParamName String Name of the lat parameter
   * @param lonParamName String Name of the lon parameter
   * @param depthParamName String Name of the depth parameter
   * @param latConstraint DoubleConstraint Constraint on latitude
   * @param lonConstraint DoubleConstraint Constraint on longitude
   * @param depthConstraint DoubleConstraint Constraint on depth
   * @param latUnits String Lat param units
   * @param lonUnits String Lon param units
   * @param depthUnits String depth param units
   * @param latValue Double lat param value, should be within the lat param constraint
   * @param lonValue Double lon param value, should be within the lon param constraint
   * @param depthValue Double depth param value, should be within the depth param constraint
   */
  public LocationParameter(String locationParamName,
                           String latParamName, String lonParamName,
                           String depthParamName,
                           DoubleConstraint latConstraint,
                           DoubleConstraint lonConstraint,
                           DoubleConstraint depthConstraint,
                           String latUnits, String lonUnits, String depthUnits,
                           Double latValue,
                           Double lonValue, Double depthValue) {

    super(locationParamName,null,null,null);
    latParam = new DoubleParameter(latParamName, latConstraint,
                                   latUnits, latValue);
    lonParam = new DoubleParameter(lonParamName, lonConstraint,
                                   lonUnits, lonValue);
    depthParam = new DoubleParameter(depthParamName,
                                     depthConstraint, depthUnits, depthValue);

    ParameterList paramList = new ParameterList();
    paramList.addParameter(latParam);
    paramList.addParameter(lonParam);
    paramList.addParameter(depthParam);
    locationParameterListParameter = new ParameterListParameter(
      LOCATION_PARAMETER_LIST_PARAMETER_NAME,
      paramList);
    location = new Location(((Double)latParam.getValue()).doubleValue(),
                                ((Double)lonParam.getValue()).doubleValue(),
                                ((Double)depthParam.getValue()).doubleValue());
    setValue(location);
    //setting the independent parameters for the Location parameter
    setIndependentParameters(paramList);
  }


  /**
   * Creates a location parameter as parameterlist parameter. This creates
   * a location parameter that holds lat param,lon param and depth parameter
   * in a parameterListParameter.
   * @param locationParamName String Parameter Name
   * @param latParamName String Name of the lat parameter
   * @param lonParamName String Name of the lon parameter
   * @param depthParamName String Name of the depth parameter
   * @param minLat double minimum lat value
   * @param maxLat double maximum lat value
   * @param minLon double minimum lon value
   * @param maxLon double maximum lon value
   * @param minDepth double mimimum depth value
   * @param maxDepth double maximum depth value
   * @param latUnits String Latitude param units
   * @param lonUnits String Longitude param units
   * @param depthUnits String Depth param units
   * @param latValue Double latitude value, should be within the minimum and
   * maximum latitude range.
   * @param lonValue Double longitude value, should be within the minimum and
   * maximum longitude range.
   * @param depthValue Double depth value, should be within the minimum and maximum
   * depth range.
   */
  public LocationParameter(String locationParamName,
                           String latParamName, String lonParamName,
                           String depthParamName,
                           double minLat,
                           double maxLat,
                           double minLon,
                           double maxLon,
                           double minDepth,
                           double maxDepth,
                           String latUnits, String lonUnits, String depthUnits,
                           Double latValue,
                           Double lonValue, Double depthValue) {
    super(locationParamName,null,null,null);
    latParam = new DoubleParameter(latParamName, new DoubleConstraint(minLat,maxLat),
                                   latUnits, latValue);
    lonParam = new DoubleParameter(lonParamName, new DoubleConstraint(minLon,maxLon),
                                   lonUnits, lonValue);
    depthParam = new DoubleParameter(depthParamName,
                                     new DoubleConstraint(minDepth,maxDepth), depthUnits, depthValue);

    ParameterList paramList = new ParameterList();
    paramList.addParameter(latParam);
    paramList.addParameter(lonParam);
    paramList.addParameter(depthParam);
    locationParameterListParameter = new ParameterListParameter(
      LOCATION_PARAMETER_LIST_PARAMETER_NAME,
      paramList);
    location = new Location(((Double)latParam.getValue()).doubleValue(),
                                ((Double)lonParam.getValue()).doubleValue(),
                                ((Double)depthParam.getValue()).doubleValue());
    setValue(location);
    //setting the independent parameters for the Location parameter
    setIndependentParameters(paramList);
  }


  /**
   * Creates a location parameter as parameterlist parameter. This creates
   * a location parameter that holds lat param,lon param and depth parameter
   * in a parameterListParameter.
   * @param locationParamName String Parameter Name
   * @param latParamName String Name of the Lat param
   * @param lonParamName String Name of the Lon param
   * @param depthParamName String Name of the depth param
   * @param latUnits String Latitudes param units
   * @param lonUnits String Longitude param units
   * @param depthUnits String Depth param  units
   * @param latValue Double valid Latitude value
   * @param lonValue Double valid longitude value
   * @param depthValue Double valid depth value
   */
  public LocationParameter(String locationParamName,
                           String latParamName, String lonParamName,
                           String depthParamName,
                           String latUnits, String lonUnits, String depthUnits,
                           Double latValue,
                           Double lonValue, Double depthValue) {
    super(locationParamName,null,null,null);
    latParam = new DoubleParameter(latParamName,
                                   new DoubleConstraint(Location.MIN_LAT,Location.MAX_LAT),
                                   latUnits, latValue);
    lonParam = new DoubleParameter(lonParamName,
                                   new DoubleConstraint(Location.MIN_LON,Location.MAX_LON),
                                   lonUnits, lonValue);
    depthParam = new DoubleParameter(depthParamName,
                                     new DoubleConstraint(Location.MIN_DEPTH,50.0),
        depthUnits, depthValue);

    ParameterList paramList = new ParameterList();
    paramList.addParameter(latParam);
    paramList.addParameter(lonParam);
    paramList.addParameter(depthParam);
    locationParameterListParameter = new ParameterListParameter(
      LOCATION_PARAMETER_LIST_PARAMETER_NAME,
      paramList);
    location = new Location(((Double)latParam.getValue()).doubleValue(),
                                ((Double)lonParam.getValue()).doubleValue(),
                                ((Double)depthParam.getValue()).doubleValue());
    setValue(location);
    //setting the independent parameters for the Location parameter
    setIndependentParameters(paramList);
  }

  /**
   * Creates a location parameter as parameterlist parameter. This creates
   * a location parameter that holds lat param,lon param and depth parameter
   * in a parameterListParameter.
   * @param locationParamName String Parameter Name
   * @param latParamName String Name of the lat param
   * @param lonParamName String Name of the lon param
   * @param depthParamName String Name of the depth param
   * @param latValue Double valid Latitude value
   * @param lonValue Double valid longitude value
   * @param depthValue Double valid depth value
   */
  public LocationParameter(String locationParamName,
                           String latParamName, String lonParamName,
                           String depthParamName,
                           Double latValue,
                           Double lonValue, Double depthValue) {

    super(locationParamName,null,null,null);
    latParam = new DoubleParameter(latParamName,
                                   new DoubleConstraint(Location.MIN_LAT,Location.MAX_LAT),
                                   "degrees", latValue);
    lonParam = new DoubleParameter(lonParamName,
                                   new DoubleConstraint(Location.MIN_LON,Location.MAX_LON),
                                   "degrees", lonValue);
    depthParam = new DoubleParameter(depthParamName,
                                     new DoubleConstraint(Location.MIN_DEPTH,50),
                                     "kms", depthValue);

    ParameterList paramList = new ParameterList();
    paramList.addParameter(latParam);
    paramList.addParameter(lonParam);
    paramList.addParameter(depthParam);
    locationParameterListParameter = new ParameterListParameter(
      LOCATION_PARAMETER_LIST_PARAMETER_NAME,
      paramList);
    location = new Location(((Double)latParam.getValue()).doubleValue(),
                                ((Double)lonParam.getValue()).doubleValue(),
                                ((Double)depthParam.getValue()).doubleValue());
    setValue(location);
    //setting the independent parameters for the Location parameter
    setIndependentParameters(paramList);
  }

  /**
   * Creates a location parameter as parameterlist parameter. This creates
   * a location parameter that holds lat param,lon param and depth parameter
   * in a parameterListParameter.
   * @param locationParamName String Parameter Name
   * @param latParamName String Name of the lat param
   * @param lonParamName String Name of the lon param
   * @param depthParamName String Name of the depth param
   * @param latConstraint DoubleConstraint lat param constraint
   * @param lonConstraint DoubleConstraint lon param constraint
   * @param depthConstraint DoubleConstraint depth param constraint
   * @param latValue Double valid latitude
   * @param lonValue Double valid longitude
   * @param depthValue Double valid depth
   */
  public LocationParameter(String locationParamName,
                            String latParamName, String lonParamName,
                            String depthParamName,
                            DoubleConstraint latConstraint,
                            DoubleConstraint lonConstraint,
                            DoubleConstraint depthConstraint,
                            Double latValue,
                            Double lonValue, Double depthValue) {
     super(locationParamName,null,null,null);
     latParam = new DoubleParameter(latParamName, latConstraint,
                                    "Degrees", latValue);
     lonParam = new DoubleParameter(lonParamName, lonConstraint,
                                    "Degrees", lonValue);
     depthParam = new DoubleParameter(depthParamName,
                                      depthConstraint, "Kms", depthValue);

     ParameterList paramList = new ParameterList();
     paramList.addParameter(latParam);
     paramList.addParameter(lonParam);
     paramList.addParameter(depthParam);
     locationParameterListParameter = new ParameterListParameter(
      LOCATION_PARAMETER_LIST_PARAMETER_NAME,
      paramList);
     location = new Location(((Double)latParam.getValue()).doubleValue(),
                                 ((Double)lonParam.getValue()).doubleValue(),
                                 ((Double)depthParam.getValue()).doubleValue());
     setValue(location);
     //setting the independent parameters for the Location parameter
     setIndependentParameters(paramList);
  }

  /**
   * Creates a location parameter as parameterlist parameter. This creates
   * a location parameter that holds lat param,lon param and depth parameter
   * in a parameterListParameter.
   * @param locationParamName String Parameter Name
   * @param latParamName String Name of the Lat Param
   * @param lonParamName String Name of the Lon param
   * @param depthParamName String Name of the depth param
   * @param minLat double minimum latitude
   * @param maxLat double maximum latitude
   * @param minLon double minimum longitude
   * @param maxLon double maximum longitude
   * @param minDepth double minimum depth
   * @param maxDepth double maximum depth
   * @param latValue Double default latitude value, should be within the min and
   * max latitude range.
   * @param lonValue Double default longitude value, should be within the min and
   * max longitude range.
   * @param depthValue Double default depth value, should be within the min and max
   * depth range.
   */
  public LocationParameter(String locationParamName,
                           String latParamName, String lonParamName,
                           String depthParamName,
                           double minLat,
                           double maxLat,
                           double minLon,
                           double maxLon,
                           double minDepth,
                           double maxDepth,
                           Double latValue,
                           Double lonValue, Double depthValue) {
    super(locationParamName,null,null,null);
    latParam = new DoubleParameter(latParamName, new DoubleConstraint(minLat,maxLat),
                                   "Degrees", latValue);
    lonParam = new DoubleParameter(lonParamName, new DoubleConstraint(minLon,maxLon),
                                   "Degrees", lonValue);
    depthParam = new DoubleParameter(depthParamName,
                                     new DoubleConstraint(minDepth,maxDepth), "Kms", depthValue);

    ParameterList paramList = new ParameterList();
    paramList.addParameter(latParam);
    paramList.addParameter(lonParam);
    paramList.addParameter(depthParam);
    locationParameterListParameter = new ParameterListParameter(
      LOCATION_PARAMETER_LIST_PARAMETER_NAME,
      paramList);

    location = new Location(((Double)latParam.getValue()).doubleValue(),
                                ((Double)lonParam.getValue()).doubleValue(),
                                ((Double)depthParam.getValue()).doubleValue());
    setValue(location);
    //setting the independent parameters for the Location parameter
    setIndependentParameters(paramList);
  }

  /**
   * No constraints specified, all values allowed. Sets the name and value.
   *
   * @param  name   Name of the parameter
   * @param  paramList  ParameterList  object
   * @param loc Location Location object
   */
  public LocationParameter(String name, ParameterList paramList,Location loc) {
    super(name,null,null,null);
    locationParameterListParameter = new ParameterListParameter(
      LOCATION_PARAMETER_LIST_PARAMETER_NAME,
      paramList);
    location = loc;
    setValue(location);
    //setting the independent parameters for the Location parameter
    setIndependentParameters(paramList);
  }


  /**
   * returns location parameter.
   * @return ParameterAPI : Returns the instance ParameterListParameter that holds the
   * parameters constituting the location, if location parameter specifies no constraint.
   * But if constraint is not null, then it returns the instance of LocationParameter with constraints,
   * similar to String parameter with constraints.
   */
  public ParameterAPI getLocationParameter(){
    if(constraint == null)
      return locationParameterListParameter;
    else
      return this;
  }


  /**
   * Sets the constraint reference if it is a StringConstraint
   * and the parameter is currently editable, else throws an exception.
   */
  public void setConstraint(ParameterConstraintAPI constraint) throws ParameterException, EditableException{

      String S = C + ": setConstraint(): ";
      checkEditable(S);

      if ( !(constraint instanceof LocationConstraint )) {
          throw new ParameterException( S +
              "This parameter only accepts LocationConstraints, unable to set the constraint."
          );
      }
      else super.setConstraint( constraint );
  }


  /**
   * Sets the location parameter with updated location value
   * @param loc Location
   */
  public void setValue(Location loc){
    location = loc;
    setValue((Object)loc);
  }


  /**
   * Returns the latitude of selected location
   * @return double
   */
  public double getLatitude(){
    if(constraint !=null)
      return location.getLatitude();
    else
      return ((Double)latParam.getValue()).doubleValue();
  }

  /**
   * Returns the longitude of the selected location
   * @return double
   */
  public double getLongitude(){
    if(constraint !=null)
      return location.getLongitude();
    else
      return ((Double)lonParam.getValue()).doubleValue();
  }


  /**
   * Returns the depth of the selected location
   * @return double
   */
  public double getDepth(){
    if(constraint !=null)
      return location.getDepth();
    else
      return ((Double)depthParam.getValue()).doubleValue();
  }

  /**
   * Returns a clone of the allowed strings of the constraint.
   * Useful for presenting in a picklist
   * @return    The allowed Locations list
   */
  public ArrayList getAllowedLocations() {
      return ( ( LocationConstraint ) this.constraint ).getAllowedLocations();
  }




  /**
   *  Compares the values to if this is less than, equal to, or greater than
   *  the comparing objects.
   *
   * @param  obj                     The object to compare this to
   * @return                         -1 if this value < obj value, 0 if equal,
   *      +1 if this value > obj value
   * @exception  ClassCastException  Is thrown if the comparing object is not
   *      a LocationParameter.
   */
  public int compareTo( Object obj ) {
    String S = C + ":compareTo(): ";

    if (! (obj instanceof LocationParameter)) {
      throw new ClassCastException(S +
          "Object not a LocationParameter, unable to compare");
    }

    LocationParameter param = (LocationParameter) obj;

    if ( (this.value == null) && (param.value == null))return 0;
    int result = 0;

    LocationParameter n1 = (LocationParameter)this.getValue();
    LocationParameter n2 = (LocationParameter) param.getValue();

    return n1.compareTo(n2);

  }


  /**
   * Compares value to see if equal.
   *
   * @param  obj                     The object to compare this to
   * @return                         True if the values are identical
   * @exception  ClassCastException  Is thrown if the comparing object is not
   *      a LocationParameter.
   */
  public boolean equals( Object obj ) {
    String S = C + ":equals(): ";

    if (! (obj instanceof LocationParameter)) {
      throw new ClassCastException(S +
          "Object not a LocationParameter, unable to compare");
    }

    String otherName = ( (LocationParameter) obj).getName();
    if ( (compareTo(obj) == 0) && getName().equals(otherName)) {
      return true;
    }
    else {
      return false;
    }
  }


  /**
   *  Returns a copy so you can't edit or damage the origial.
   *
   * @return    Exact copy of this object's state
   */
  public Object clone() {
    LocationConstraint c1 = null;
    LocationParameter param = null;
    if (constraint != null){
      c1 = (LocationConstraint) constraint.clone();
      param = new LocationParameter(name, c1,(Location)value);
    }
    else{
      if(value != null)
        param = new LocationParameter(name,
                                      (ParameterList)locationParameterListParameter.getValue(),
                                      (Location)value);
      else
        param = new LocationParameter(name);
    }
    param.editable = true;
    return param;

  }

  /**
   * Returns the ListIterator of the parameters included within this parameter
   * @return
   */
  public ListIterator getParametersIterator(){
    return ((ParameterList)this.getValue()).getParametersIterator();
  }

  /**
   *
   * @returns the parameterList contained in this parameter
   */
  public ParameterList getParameter(){
    return (ParameterList)getValue();
  }

  /**
   * Returns the name of the parameter class
   */
  public String getType() {
    String type;
    if (constraint != null) type = "Constrained" + PARAM_TYPE;
    else type = PARAM_TYPE;
    return type;
  }

  /**
   * This overrides the getmetadataString() method because the value here
   * does not have an ASCII representation (and we need to know the values
   * of the independent parameter instead).
   * @returns String
   */
  public String getMetadataString() {
    if(constraint == null)
      return getDependentParamMetadataString();
    else //get the Metadata for the location parameter if it is a single parameter
      //similar to constraint string parameter.
      return super.getMetadataString();
  }


}


