package org.scec.exceptions;

/**
 *  <b>Title:</b> WarningException<br>
 *  <b>Description:</b> Exception thrown when WarningListener set's a
 *  parameter value, to overide calling ParameterChangeEvent
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */

public final class WarningException extends RuntimeException {

    /**
     *  No-arg constructor
     */
    public WarningException() {
        super();
    }


    /**
     *  Constructor that specifies an error message
     *
     * @param  string  Describes the error
     */
    public WarningException( String string ) {
        super( string );
    }
}
