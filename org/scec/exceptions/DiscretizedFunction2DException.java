/*
 *  put your module comment here
 *  formatted with JxBeauty (c) johann.langhofer@nextra.at
 */

package org.scec.exceptions;

/**
 *  <b>Title:</b> DiscretizedFunction2DException<br>
 *  <b>Description:</b> Exception thrown when DiscretizedFunction2D errors occur
 *  <br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */
public class DiscretizedFunction2DException extends RuntimeException {

    /**
     *  No-arg constructor
     */
    public DiscretizedFunction2DException() { }


    /**
     *  Constructor that specifies an error message
     *
     * @param  string  Describes the error
     */
    public DiscretizedFunction2DException( String string ) {
        super( string );
    }
}


