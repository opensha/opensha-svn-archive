package org.scec.exceptions;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class EditableException extends RuntimeException {

    /**
     *  No-arg constructor
     */
    public EditableException() { }


    /**
     *  Constructor that specifies an error message
     *
     * @param  string  Describes the error
     */
    public EditableException( String string ) {
        super( string );
    }
}
