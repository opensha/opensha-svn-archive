package org.scec.exceptions;

/**
 *  <b>Title:</b> DataPoint2DException<br>
 *  <b>Description:</b> Exception thrown when DataPoint2D errors occur<br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */
public class DataPoint2DException extends RuntimeException {

    /**
     *  No-arg constructor
     */
    public DataPoint2DException() { }


    /**
     *  Constructor that specifies an error message
     *
     * @param  string  Describes the error
     */
    public DataPoint2DException( String string ) {
        super( string );
    }
}

