package org.scec.exceptions;

/**
 *  <b>Title:</b> InvalidArrayShapeException<p>
 *  <b>Description:</b> Exception thrown when trying to set invalid sizes to any
 *  2D grid i nthe gridded fault framework. <p>
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */
public final class InvalidArrayShapeException extends RuntimeException {

    /**
     *  No-arg constructor
     */
    public InvalidArrayShapeException() { }


   /**
     *  Constructor that specifies an error message
     *
     * @param  string  Describes the error
     */
    public InvalidArrayShapeException( String string ) {
        super( string );
    }
}


