package org.scec.exceptions;

/**
 * <b>Title:</b> EditableException<p>
 *
 * <b>Description:</b> SWR: I have no idea what this is used for since
 * I didn't create the class, and the creator left no comments<p>
 *
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
