package org.scec.param;

import org.scec.param.translate.*;
import java.util.*;
import org.scec.param.event.*;
import org.scec.exceptions.*;

/**
 * <p>Title: TranslatedWarningDoubleParameter</p>
 * <p>Description: Will translate the values when setting and getting,
 * unless the translation is disabled. It that case it will act just like a
 * normal WarningDoubleParameter. The translator can be any function command
 * that you pass in. Default is LogTranslator(). Initially set translate to
 * true. </p>
 *
 * This class allows a user to input normal values that get translated internally.
 * Then these values can be inputted to a process that needs the translated values.
 * This is real helpful for user input of log values. Since the translation
 * can be disabled, it is really easy to get to the underlying real data.<p>
 *
 * Note: The logical purpose of this class is to input values in one form,
 * then translate them to the desired form, and use the translated values
 * in a calculation. So with this in mind it is really the input values that
 * are translated, and the translator puts them back in the correct form.<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public class TranslatedWarningDoubleParameter
    implements WarningParameterAPI, DependentParameterAPI, ParameterAPI
{


    /** Class name for debugging. */
    protected final static String C = "TranslatedWarningDoubleParameter";
    /** If true print out debug statements. */
    protected final static boolean D = true;


    /**
     * Default translator is LogTranslator. The translator class is basically
     * a pointer to a function with translate and reverse operations.
     */
    protected TranslatorAPI trans = new LogTranslator();

    /**
     * If true, translation will occur on get, and setValue operations. If false
     * this class acts as a passthrough to the underlying WarningDoubleParameter,
     * in other words this subclass becomes transparent and has no affect.
     */
    protected boolean translate = true;

    /** Returns true if translation is in affect, false otherwise */
    public boolean isTranslate() { return translate; }
    /**
     * Public api that allows enabling or disabling translation. If disabled,
     * this class acts as if it was transparent, i.e. it does noting, just provides
     * passthrough to the underlying WarningDoubleParameter functions.
     */
    public void setTranslate(boolean translate) { this.translate = translate; }

    /** Internal reference to the wrapped parameter */
    protected WarningDoubleParameter param = null;

    /**
     * Allows setting the parameter upon construction. The translator defaults to
     * the LogTranslator. This form allows passing in normal values that get
     * translated to log values and set in the underlying parameter.<p>
     *
     * Note: No translation changes are applied to the passed in parameter. It is
     * assumed that the parameter is already in translated log space.
     */
    public TranslatedWarningDoubleParameter( WarningDoubleParameter param ) { this.param = param; }

    /**
     * Allows setting the translator upon construction. Overides the default LogTranslator.
     * This provides the ability to set the translator to anything you want. Such
     * examples include SinTranslator and AbsTranslator, which takes the sin() and
     * absolute value respectivly. <p>
     *
     * Note that ths SinTranslator is an example of a one-to-one mapping. Reverse can
     * restore any data. AbsTranslator is unrecoverable, it cannot restore negative
     * values, only positive ones.
     *
     */
    public TranslatedWarningDoubleParameter( TranslatorAPI trans ) { this(null, trans); }

    /**
     * Allows setting the parameter for translation access, and setting
     * the translator function upon construction.<p>
     *
     * Note: No translation changes are applied to the passed in parameter. It is
     * assumed that the parameter is already in translated space.
     */
    public TranslatedWarningDoubleParameter( WarningDoubleParameter param, TranslatorAPI trans ) {
        this.trans = trans;
        this.param = param;
    }

    /**
     * Public access to the wrapped parameter, allows setting the parameter.<p>
     *
     * Note: No translation changes are applied to the passed in parameter. It is
     * assumed that the parameter is already in translated space.
     */
    public void setParameter(WarningDoubleParameter param){ this.param = param; }
    /**
     * Public access to the wrapped parameter, allows getting the parameter.
     * This is useful for normal access to the parameter in translated space.<p>
     *
     * Note: No translation changes are applied to the fetched parameter. It is
     * assumed that the parameter is returned in translated space.
     */
    public WarningDoubleParameter getParameter(){ return param; }



    /**
     *  Gets the min value of the constraint object. Does a reverse translation
     *  on the underlying Parameter data if the translate flag is set.
     *
     * @return                The reverse translated min value.
     * @exception  Exception  Thrown if any mathmatical exceptions occur.
     */
    public Double getWarningMin() throws TranslateException, Exception {
        Double min = param.getWarningMin();
        if( min  == null || !translate ) return min;
        else return new Double( trans.reverse( min.doubleValue() ) );
    }


    /**
     *  Translated proxy values to constraint check when setting a value.
     *
     * @param  value  Description of the Parameter
     * @return        The allowed value
     */
    public boolean isAllowed( Object value ){

        if( value == null || ( value instanceof Double) || !translate  ){
            return param.isAllowed( value );
        }
        else{
            double d = ((Double)value).doubleValue();
            return param.isAllowed( trans.translate( d ) );
        }

    }

    /**
     *  Gets the max value of the constraint object. Does a reverse translation
     *  on the underlying Parameter data if the translate flag is set.
     *
     * @return                The reverse translated max value.
     * @exception  Exception  Thrown if any mathmatical exceptions occur.
     */
    public Double getWarningMax() throws TranslateException {
        Double max = param.getWarningMax();
        if( max  == null || !translate ) return max;
        else return new Double( trans.reverse( max.doubleValue() ) );
    }


    /**
     *  Set's the parameter's value in the underlying parameter. Translation is
     *  performed on the value if the translate flag is set before passing to the
     *  WarningDoubleParameter. Note, if this object is not a Double, it is passed
     *  through without translation. WarningDoubleParameter constraint will fail.
     *
     * @param  value                 The new value for this Parameter
     * @throws  ParameterException   Thrown if the object is currenlty not
     *      editable
     * @throws  ConstraintException  Thrown if the object value is not allowed
     */
    public synchronized void setValue( Object value ) throws ConstraintException, WarningException {
        String S = C + ": setValue(): ";
        if(D) System.out.println(S + "Starting: ");

        if ( value == null  || !translate ||  !( value instanceof Double ) )
            param.setValue( value );
        else{

            Double dUntranslated = (Double)value;
            Double dTranslated = new Double( trans.translate( dUntranslated.doubleValue() ) );

            if( !param.isAllowed( dTranslated ) ) {
                String err = S + "Value is not allowed: ";
                if( value != null ) err += dUntranslated.toString();
                if(D) System.out.println(err);
                throw new ConstraintException( err );
            }
            else if ( !param.isRecommended( dTranslated ) ) {

                if(D) System.out.println(S + "Firing Warning Event");

                ParameterChangeWarningEvent event = new
                      ParameterChangeWarningEvent( (Object)this, this, this.getValue(), dUntranslated );

                fireParameterChangeWarning( event );
                throw new WarningException( S + "Value is not recommended: " + dUntranslated.toString() );
            }
            else {
                if(D) System.out.println(S + "Setting allowed and recommended value: ");
                param.setValue( dTranslated );
            }
        }
    }




    /**
     *  Returns the parameter's value. Each subclass defines what type of
     *  object. it returns
     *
     * @return    The value value
     */
    public Object getValue() {

        Object value = param.getValue();
        if ( value == null || !translate || !(value instanceof Double) ) return value;
        else{
            double d = ((Double)value).doubleValue();
            d = trans.reverse( d );
            return new Double( d ) ;
        }
    }


    /**
     *  Set's the parameter's value in the underlying parameter. Translation is
     *  performed on the value if the translate flag is set before passing to the
     *  WarningDoubleParameter. The warning constraints are ignored. <p>
     *
     *  Note, if this object is not a Double, it is passed
     *  through without translation. WarningDoubleParameter constraint will fail.
     *
     * @param  value                 The new value for this Parameter
     * @throws  ParameterException   Thrown if the object is currenlty not
     *      editable
     * @throws  ConstraintException  Thrown if the object value is not allowed
     */
    public void setValueIgnoreWarning( Object value ) throws ConstraintException, ParameterException {
        String S = C + ": setValueIgnoreWarning(): ";
        if(D) System.out.println(S + "Setting value ignoring warning and constraint: ");

        if ( value == null || !translate || !( value instanceof Double ) )
            param.setValueIgnoreWarning( value );
        else{
            double d = trans.translate( ((Double)value).doubleValue() );
            param.setValueIgnoreWarning( new Double( d ) );
        }
    }


    /**
     *  Uses the constraint object to determine if the new value being set is
     *  within recommended range. If no Constraints are present all values are recommended.
     *  Translation is performed on the value if the translate flag is
     *  set before passing to the WarningDoubleParameter function.
     *
     * @param  obj  Object to check if allowed via constraints
     * @return      True if the value is allowed
     */
    public boolean isRecommended( Object obj ) {

        if ( obj == null || !translate || !( obj instanceof Double ) ) return param.isRecommended( obj );
        else{
            double d = trans.translate( ((Double)obj).doubleValue() );
            return param.isRecommended( new Double( d ) );
        }

    }


    /**
     *  Gets the min value of the constraint object. Does a reverse translation
     *  on the underlying Parameter data if the translate flag is set.
     *
     * @return                The min value
     * @exception  Exception  Description of the Exception
     */
    public Double getMin() throws Exception {

        Double min = param.getMin();
        if( min  == null || !translate ) return min;
        else return new Double( trans.reverse( min.doubleValue() ) );

    }


    /**
     *  Returns the maximum allowed value of the constraint object.
     *  Does a reverse translation on the underlying Parameter
     *  data if the translate flag is set.
     *
     * @return    The max value
     */
    public Double getMax() {

        Double max = param.getMax();
        if( max  == null || !translate ) return max;
        else return new Double(  trans.reverse( max.doubleValue() ) );

    }


    // *******************************************
    // *******************************************
    // These function are not translated
    // *******************************************
    // *******************************************

    public void setIgnoreWarning(boolean ignoreWarning) {
        param.setIgnoreWarning(ignoreWarning);
    }
    public boolean isIgnoreWarning() { return param.isIgnoreWarning(); }

    // *******************************************
    // WarningDoubleParameterAPI Proxy methods
    // *******************************************

    /**
     *  Sets the constraint if it is a StringConstraint and the parameter
     *  is currently editable.
     */
    public void setWarningConstraint(DoubleConstraint warningConstraint){
        param.setWarningConstraint(warningConstraint); }

    /**
     *  Sets the constraint if it is a StringConstraint and the parameter
     *  is currently editable.
     */
    public DoubleConstraint getWarningConstraint() throws ParameterException{
        return param.getWarningConstraint();}


    /**
     *  Adds a feature to the ParameterChangeFailListener attribute of the
     *  ParameterEditor object
     *
     * @param  listener  The feature to be added to the
     *      ParameterChangeFailListener attribute
     */
    public void addParameterChangeWarningListener( ParameterChangeWarningListener listener ){
        param.addParameterChangeWarningListener( listener ) ;}

    /**
     *  Description of the Method
     *
     * @param  listener  Description of the Parameter
     */
    public void removeParameterChangeWarningListener( ParameterChangeWarningListener listener ){
        param.removeParameterChangeWarningListener( listener ) ;}



    /**
     *  Description of the Method
     *
     * @param  event  Description of the Parameter
     */
    public void fireParameterChangeWarning( ParameterChangeWarningEvent event ){
        param.fireParameterChangeWarning( event ) ;}


    /**
     *  Compares value to see if equal.
     *
     * @param  obj                     The object to compare this to
     * @return                         True if the values are identical
     * @exception  ClassCastException  Is thrown if the comparing object is not
     *      a DoubleParameter, or DoubleDiscreteParameter.
     */
    public boolean equals( Object obj ) throws ClassCastException { return param.equals( obj ) ;}

    /**
     *  Returns a copy so you can't edit or damage the origial.
     *
     * @return    Exact copy of this object's state
     */
    public Object clone(){ return param.clone();}




    // *******************************************
    // DependentParameterAP Proxy methods
    // *******************************************

    // ListIterator guarantees the order that you add parameters
    public ListIterator getIndependentParametersIterator(){
        return param.getIndependentParametersIterator();}

    public ParameterAPI getIndependentParameter(String name)throws ParameterException{
        return param.getIndependentParameter(name);}

    // This will clone the parameters of the list that you pass in
    public void setIndependentParameters(ParameterList list){
        param.setIndependentParameters(list);}

    /** Adds the parameter if it doesn't exist, else throws exception */
    public void addIndependentParameter(ParameterAPI parameter) throws ParameterException{
        param.addIndependentParameter(parameter) ;}

    public boolean containsIndependentParameter(String name){
        return param.containsIndependentParameter(name) ;}

    public void removeIndependentParameter(String name) throws ParameterException{
        param.removeIndependentParameter(name) ;}

    public String getIndependentParametersKey(){
        return param.getIndependentParametersKey() ;}




    // *******************************************
    // ParameterAP Proxy methods
    // *******************************************



    /**
     *  Every parameter has a name, this function returns that name.
     *
     * @return    The name value
     */
    public String getName(){ return param.getName();}

    /**
     *  Every parameter has a name, this function returns that name.
     *
     * @return    The name value
     */
    public void setName(String name){ param.setName(name);}

    /**
     *  Every parameter constraint has a name, this function sets that name.
     *  Defaults to the name of the parameter
     *
     * @return    The name value
     */
    public String getConstraintName(  ){ return param.getConstraintName();}

    /**
     *  Gets the constraints of this parameter. Each subclass may implement any
     *  type of constraint it likes. This version returns a clone with reverse
     *  translated min and max values.
     *
     * @return    The constraint value
     */
    public ParameterConstraintAPI getConstraint(){

        if( param.getConstraint() == null || !translate ) return param.getConstraint();
        DoubleConstraint constraint = (DoubleConstraint)param.getConstraint();

        double transMin = trans.reverse( constraint.getMin().doubleValue() );
        double transMax = trans.reverse( constraint.getMax().doubleValue() );
        DoubleConstraint constraint2 =  new DoubleConstraint(transMin, transMax);
        return constraint2;

    }

    /**
     *  Gets the constraints of this parameter. Each subclass may implement any
     *  type of constraint it likes.
     *
     * @return    The constraint value
     */
    public void setConstraint(ParameterConstraintAPI constraint){ param.setConstraint(constraint); }


    /**
     *  Returns the units of this parameter, represented as a String.
     *
     * @return    The units value
     */
    public String getUnits(){ return param.getUnits();}

    /**
     * Sets the units of this parameter
     * @param units
     */
    public void setUnits(String units){ param.setUnits(units);}

    /**
     *  Returns a description of this Parameter, typically used for tooltips.
     *
     * @return    The info value
     */
    public String getInfo(){ return param.getInfo();}


    /**
     *  Sets the info attribute of the ParameterAPI object.
     *
     * @param  info  The new info value
     */
    public void setInfo( String info ){ param.setInfo( info );}


    /**
     *  Returns the data type of the value object. Used to determine which type
     *  of Editor to use in a GUI.
     *
     * @return    The type value
     */
    public String getType(){ return "TranslatedWarningDoubleParameter";}


    /**
     *  Compares the values to see if they are the same. Returns -1 if obj is
     *  less than this object, 0 if they are equal in value, or +1 if the object
     *  is greater than this.
     *
     * @param  parameter            the parameter to compare this object to.
     * @return                      -1 if this value < obj value, 0 if equal, +1
     *      if this value > obj value
     * @throws  ClassCastException  Thrown if the object type of the parameter
     *      argument are not the same.
     */
    public int compareTo( Object parameter ) throws ClassCastException{ return param.compareTo( parameter );}


    /**
     *  Compares value to see if equal.
     *
     * @param  parameter            the parameter to compare this object to.
     * @return                      True if the values are identical
     * @throws  ClassCastException  Thrown if the object type of the parameter
     *      argument are not the same.
     */
    //public boolean equals( Object parameter ) throws ClassCastException{ return param.equals( parameter );}





    /**
     *  Determines if the value can be edited, i.e. changed once set.
     *
     * @return    The editable value
     */
    public boolean isEditable(){ return param.isEditable();}


    /**
     *  Disables editing the value once it is set.
     */
    public void setNonEditable(){ param.setNonEditable();}


    /**
     *  Returns a copy so you can't edit or damage the origial.
     *
     * @return    Description of the Return Value
     * /
    public Object clone(){
        return param.clone();}
    */

    public boolean isNullAllowed(){ return param.isNullAllowed();}
    public TranslatorAPI getTrans() {
        return trans;
    }

}