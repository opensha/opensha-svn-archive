package org.opensha.commons.exceptions;

/**
 *  <b>Title:</b> LocationException<p>
 *  <b>Description:</b> Exception thrown when Location errors occur,
 *  such as trying to set invalid depths, such as a negative number<p>
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
public class LocationException extends RuntimeException {

	private static final long serialVersionUID = 0x55A0A7B;
	
    /** No-arg constructor */
    public LocationException()  { super(); }
    /** Constructor that specifies an error message */
    public LocationException( String string ) { super( string ); }
}


