/*
 *  put your module comment here
 *  formatted with JxBeauty (c) johann.langhofer@nextra.at
 */

package org.scec.exceptions;

/**
 *  <b>Title:</b> NamedObjectException<br>
 *  <b>Description:</b> Exception thrown when NamedObject errors occur<br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
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


