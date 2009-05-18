package org.opensha.commons.exceptions;

/**
 *  <b>Title:</b> NotResizableException<p>
 *
 *  <b>Description:</b> Exception thrown when trying to resize data structures
 *  that are not resizable, such as a GriddedSurface<p>
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
public final class NotResizableException extends RuntimeException {

	private static final long serialVersionUID = 0x64354A8;
	
    /** No-arg constructor */
    public NotResizableException()  { super(); }
    /** Constructor that specifies an error message */
    public NotResizableException( String string ){ super( string ); }
}


