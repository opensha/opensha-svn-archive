/*
 *  put your module comment here
 *  formatted with JxBeauty (c) johann.langhofer@nextra.at
 */

package org.scec.exceptions;

/**
 *  <b>Title:</b> LocationException<br>
 *  <b>Description:</b> Exception thrown when Location errors occur<br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
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


