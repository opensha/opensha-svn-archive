package org.opensha.commons.exceptions;

/**
 *  <b>Title:</b> MagFreqDistException<p>
 *
 *  <b>Description:</b> Exception thrown when MagFreqDistException errors occur<p>
 *
 * Note: These exception subclasses add no new functionality. It's really
 * the class name that is the important information. The name indicates what
 * type of error it is and helps to pinpoint where the error could have occured
 * in the code. It it much easier to see different exception types than have one
 * catchall RuntimeException type.<p>
 *
 * @author     Nitin Gupta
 * @created    August, 9,2002
 * @version    1.0
 */
public class MagFreqDistException extends RuntimeException {

    /** No-arg constructor */
    public MagFreqDistException()  { super(); }
    /** Constructor that specifies an error message */
    public MagFreqDistException(String string ) { super( string ); }
}

