package org.scec.exceptions;

/**
 *  <b>Title:</b> ParameterException<br>
 *  <b>Description:</b> Exception thrown when translation errors
 *  occur. For example, if the translator is the LogTranslator,
 *  it would throw an error when trying to translate zero or
 *  negative values.<br>
 *
 * @author     Steven W. Rock
 * @created    August 8, 2002
 * @version    1.0
 */
public class TranslateException extends RuntimeException {

    /**
     *  No-arg constructor
     */
    public TranslateException() { }


    /**
     *  Constructor that specifies an error message
     *
     * @param  string  Describes the error
     */
    public TranslateException( String string ) {
        super( string );
    }
}

