package org.scec.exceptions;

/**
 *  <b>Title:</b> ConstraintException<p>
 *  <b>Description:</b> Exception thrown when constraints reject setting a
 *  Parameter value<p>
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

