package org.scec.exceptions;

/**
 *  <b>Title:</b> ParameterException<p>
 *  <b>Description:</b> Exception thrown when Parameter errors occur, such as
 *  setting a value with an invalid data type for that parameter<p> *
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */
public class ParameterException extends RuntimeException {

    /**
     *  No-arg constructor
     */
    public ParameterException() { }


    /**
     *  Constructor that specifies an error message
     *
     * @param  string  Describes the error
     */
    public ParameterException( String string ) {
        super( string );
    }
}


