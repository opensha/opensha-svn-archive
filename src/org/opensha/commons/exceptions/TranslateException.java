package org.opensha.commons.exceptions;

/**
 *  <b>Title:</b> ParameterException<br>
 *  <b>Description:</b> Exception thrown when translation errors
 *  occur. For example, if the translator is the LogTranslator,
 *  it would throw an error when trying to translate zero or
 *  negative values.<p>
 *
 * Note: These exception subclasses add no new functionality. It's really
 * the class name that is the important information. The name indicates what
 * type of error it is and helps to pinpoint where the error could have occured
 * in the code. It it much easier to see different exception types than have one
 * catchall RuntimeException type.<p>
 *
 * @author     Steven W. Rock
 * @created    August 8, 2002
 * @version    1.0
 */
public class TranslateException extends RuntimeException {

    /** No-arg constructor */
    public TranslateException() { super(); }
    /** Constructor that specifies an error message */
    public TranslateException( String string ) { super( string ); }
}

