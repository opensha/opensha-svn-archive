package org.scec.exceptions;

/**
 *  <b>Title:</b> MagFreqDistException<br>
 *  <b>Description:</b> Exception thrown when MagFreqDistException errors occur
 *  <br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
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

