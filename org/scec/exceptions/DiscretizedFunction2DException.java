package org.scec.exceptions;

/**
 *  <b>Title:</b> DiscretizedFunction2DException<p>
 *  <b>Description:</b> Exception thrown when DiscretizedFunction2D errors occur<p>
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


