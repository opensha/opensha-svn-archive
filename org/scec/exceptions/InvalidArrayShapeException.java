/*
 *  put your module comment here
 *  formatted with JxBeauty (c) johann.langhofer@nextra.at
 */

package org.scec.exceptions;

/**
 *  <b>Title:</b> InvalidArrayShapeException<br>
 *  <b>Description:</b> Exception thrown when trying to set invalid sizes to any
 *  2D grid<br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
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


