package org.scec.param.translate;

import org.scec.exceptions.TranslateException;

/**
 * <p>Title: LogTranslator</p>
 * <p>Description: Translates values into the log space and back.
 * Throws translate errors when trying to take the log of negative
 * or zero values. </p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public class LogTranslator implements TranslatorAPI {

    public double translate(double val)  throws TranslateException{
        if( val <= 0 ) throw new TranslateException("Cannot translate zero or negative values into log space.");
        return Math.log(val);
    }
    public double reverse(double val)  throws TranslateException{
        //if( val < 0 ) throw new TranslateException("Cannot reverse log negative values from log space.");
        return Math.exp( val );

    }
}