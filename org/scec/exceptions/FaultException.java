package org.scec.exceptions;

/**
 * <b>Title:</b> FaultException<p>
 * <b>Description: </b> Errors thrown when creating fault models.<p>
 *
 * @author unascribed
 * @version 1.0
 */

public class FaultException extends RuntimeException {

    /**
     *  No-arg constructor
     */
    public FaultException() { }


    /**
     *  Constructor that specifies an error message
     *
     * @param  string  Describes the error
     */
    public FaultException( String string ) {
        super( string );
    }
}
