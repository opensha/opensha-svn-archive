package org.scec.exceptions;

/**
 *  <b>Title:</b> ConstraintException<br>
 *  <b>Description:</b> Exception thrown when constraints reject setting a
 *  Parameter value<br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */

public final class ConstraintException extends RuntimeException {

    /**
     *  No-arg constructor
     */
    public ConstraintException() {
        super();
    }


    /**
     *  Constructor that specifies an error message
     *
     * @param  string  Describes the error
     */
    public ConstraintException( String string ) {
        super( string );
    }
}

