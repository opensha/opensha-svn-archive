package org.scec.exceptions;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
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
