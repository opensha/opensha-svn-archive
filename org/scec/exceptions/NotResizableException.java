package org.scec.exceptions;

/**
 *  <b>Title:</b> NotResizableException<p>
 *
 *  <b>Description:</b> Exception thrown when trying to resize data structures
 *  that are not resizable, such as a GriddedSurface<p>
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */
public final class NotResizableException extends RuntimeException {

    /**
     *  No-arg constructor
     */
    public NotResizableException() { }


    /**
     *  Constructor that specifies an error message
     *
     * @param  string  Describes the error
     */
    public NotResizableException( String string ) {
        super( string );
    }
}


