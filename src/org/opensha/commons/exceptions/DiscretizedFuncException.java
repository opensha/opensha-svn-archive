package org.opensha.commons.exceptions;

/**
 *  <b>Title:</b> DiscretizedFuncException<p>
 *  <b>Description:</b> Exception thrown when Discretized Function errors occur<p>
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
public class DiscretizedFuncException extends RuntimeException {

    /** No-arg constructor */
    public DiscretizedFuncException()  { super(); }
    /** Constructor that specifies an error message */
    public DiscretizedFuncException( String string ) { super( string ); }
}


