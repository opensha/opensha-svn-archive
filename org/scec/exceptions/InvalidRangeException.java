package org.scec.exceptions;

/**
 *  <b>Title:</b> InvalidRangeException<p>
 *  <b>Description:</b> Exception thrown when trying to set a value outside an
 *  axis range.<p>
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */
public class InvalidRangeException extends RuntimeException {

    /**
     *  No-arg constructor
     */
    public InvalidRangeException() { }



    /**
     *  Constructor that specifies an error message
     *
     * @param  string  Describes the error
     */
    public InvalidRangeException( String string ) {
        super( string );
    }
}

