package org.scec.exceptions;

/**
 *  <b>Title:</b> NamedObjectException<p>
 *
 *  <b>Description:</b> Exception thrown when NamedObject errors occur<p>
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */
public class NamedObjectException extends RuntimeException {

    /**
     *  No-arg constructor
     */
    public NamedObjectException() { }


    /**
     *  Constructor that specifies an error message
     *
     * @param  string  Describes the error
     */
    public NamedObjectException( String string ) {
        super( string );
    }
}


