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

    /**
     *  Class name for debugging.
     */
    protected final static String C = "TranslatedWarningDoubleParameter";
    /**
     *  If true print out debug statements.
     */
    protected final static boolean D = false;


    /** Default translator is LogTranslator */
    protected TranslatorAPI trans = new LogTranslator();
    protected boolean translate = true;


    public boolean isTranslate() { return translate; }
    public void setTranslate(boolean translate) { this.translate = translate; }


    protected WarningDoubleParameter param = null;

    /**
     *
     */
    public TranslatedWarningDoubleParameter( WarningDoubleParameter param ) {
        this.param = param;
    }

    /**
     *
     */
    public TranslatedWarningDoubleParameter( TranslatorAPI trans ) {
        this.trans = trans;
    }

    /**
     *
     */
    public TranslatedWarningDoubleParameter( WarningDoubleParameter param, TranslatorAPI trans ) {
        this.trans = trans;
        this.param = param;
    }


    public void setParameter(WarningDoubleParameter param){ this.param = param; }
    public WarningDoubleParameter getParameter(){ return param; }
}