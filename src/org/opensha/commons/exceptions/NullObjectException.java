package org.opensha.commons.exceptions;

/**
 *  <b>Title:</b> NullObjectException<p>
 *  <b>Description:</b> This exception occurs when a method is being invoked
 * on a null object.
 * <p>
 *
 * Note: These exception subclasses add no new functionality. It's really
 * the class name that is the important information. The name indicates what
 * type of error it is and helps to pinpoint where the error could have occured
 * in the code. It it much easier to see different exception types than have one
 * catchall Exception type.<p>
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */
public class NullObjectException extends Exception {

    /** No-arg constructor */
    public NullObjectException()  { super(); }
    /** Constructor that specifies an error message */
    public NullObjectException( String string ) { super( string ); }
}
