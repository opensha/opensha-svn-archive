package org.opensha.commons.exceptions;

/**
 *  <b>Title:</b> InvalidArrayShapeException<p>
 *  <b>Description:</b> Exception thrown when trying to set invalid sizes to any
 *  2D grid i nthe gridded fault framework. <p>
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
public final class InvalidArrayShapeException extends RuntimeException {

	private static final long serialVersionUID = 0x12EA7F9;
	
    /** No-arg constructor */
    public InvalidArrayShapeException()  { super(); }
    /** Constructor that specifies an error message */
    public InvalidArrayShapeException( String string ) { super( string ); }
}


