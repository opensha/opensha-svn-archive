package org.scec.exceptions;

/**
 *  <b>Title:</b> LocationException<p>
 *  <b>Description:</b> Exception thrown when Location errors occur,
 *  such as trying to set invalid depths, such as a negative number<p>
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */
public class LocationException extends RuntimeException {

    /**
     *  No-arg constructor
     */
    public LocationException() { }


    /**
     *  Constructor that specifies an error message
     *
     * @param  string  Describes the error
     */
    public LocationException( String string ) {
        super( string );
    }
}


