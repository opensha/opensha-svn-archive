package org.scec.exceptions;

/**
 *  <b>Title:</b> MagFreqDistException<p>
 *
 *  <b>Description:</b> Exception thrown when MagFreqDistException errors occur<p>
 *
 * @author     Nitin Gupta
 * @created    August, 9,2002
 * @version    1.0
 */
public class MagFreqDistException extends RuntimeException {

    /**
     *  No-arg constructor
     */
    public MagFreqDistException() { }


    /**
     *  Constructor that specifies an error message
     *
     * @param  string  Describes the error
     */
    public MagFreqDistException(String string ) {
        super( string );
    }
}

