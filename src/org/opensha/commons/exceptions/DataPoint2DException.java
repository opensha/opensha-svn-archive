package org.opensha.commons.exceptions;

/**
 *  <b>Title:</b> DataPoint2DException<p>
 *  <b>Description:</b> Exception thrown when DataPoint2D errors occur<p>
 *
 * Note: These exception subclasses add no new functionality. It's really
 * the class name that is the important information. The name indicates what
 * type of error it is and helps to pinpoint where the error could have occured
 * in the code. It it much easier to see different exception types than have one
 * catchall RuntimeException type.<p>
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */
public class DataPoint2DException extends RuntimeException {

    /** No-arg constructor */
    public DataPoint2DException()  { super(); }
    /** Constructor that specifies an error message */
    public DataPoint2DException( String string ) { super( string ); }
}

