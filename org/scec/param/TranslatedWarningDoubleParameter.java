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

public class TranslatedWarningDoubleParameter{

    /** Class name for debugging. */
    protected final static String C = "TranslatedWarningDoubleParameter";
    /** If true print out debug statements. */
    protected final static boolean D = false;


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
    public TranslatedWarningDoubleParameter( WarningDoubleParameter param ) { this(param, null); }

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

        if ( !translate || value == null || !( value instanceof Double ) )
            param.setValue( value );
        else{
            double d = trans.translate( ((Double)value).doubleValue() );
            param.setValue( new Double( d ) );
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
        if ( !translate || value == null || !(value instanceof Double) ) return value;
        else{
            double d = trans.reverse( ((Double)value).doubleValue() );
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

        if ( !translate || value == null || !( value instanceof Double ) )
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

        if ( !translate || obj == null || !( obj instanceof Double ) ) return param.isRecommended( obj );
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




}