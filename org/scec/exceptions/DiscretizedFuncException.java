package org.scec.exceptions;

/**
 *  <b>Title:</b> DiscretizedFuncException<p>
 *  <b>Description:</b> Exception thrown when Discretized Function errors occur<p>
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */
public class DiscretizedFuncException extends RuntimeException {

    /**
     *  No-arg constructor
     */
    public DiscretizedFuncException() { }


    /**
     *  Constructor that specifies an error message
     *
     * @param  string  Describes the error
     */
    public DiscretizedFuncException( String string ) {
        super( string );
    }
}


