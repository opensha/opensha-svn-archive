package org.scec.exceptions;

/**
 *  <b>Title:</b> ConstraintException<p>
 *
 *  <b>Description:</b> Exception thrown when constraints reject setting a
 *  Parameter value<p>
 *
 *  <b>Copyright:</b> Copyright (c) 2001<p>
 *  <b>Company:</b> <p>
 *
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */

public final class IMRException extends RuntimeException {

    /**
     *  No-arg constructor
     */
    public IMRException() { super(); }


    /**
     *  Constructor that specifies an error message
     *
     * @param  string  Describes the error
     */
    public IMRException( String string ) { super( string ); }
}
