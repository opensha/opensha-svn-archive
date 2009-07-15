package org.opensha.commons.exceptions;

/**
 * <p>Title: RegionConstraintException</p>
 *
 * <p>Description: This exception is thrown when the user throws the
 * region constraint exception.If MinLat is greater then MaxLat , similarly
 * if MinLon is greater then MaxLon then this exception is thrown.</p>
 * @author Ned Field, Nitin Gupta
 * @version 1.0
 */
public class RegionConstraintException extends Exception{

    /**
     * Constructs a new RegionConstraintException with null as its detail message.
     * he cause is not initialized, and may subsequently be initialized by a
     * call to Throwable.initCause(java.lang.Throwable).
     */
    public RegionConstraintException() {
    }

    /**
     * Constructor with String as the error message
     * Constructs a new RegionConstraintException with the specified detail message.
     * The  cause is not initialized, and may subsequently be initialized by  a
     * call to Throwable.initCause(java.lang.Throwable)
     * @param errMsg : customized error message describing the error, what caused it
     * and what can user do to avoid it.The detail message is saved for later
     * retrieval by the Throwable.getMessage() method.
     */
    public RegionConstraintException(String errMsg) {
      super(errMsg);
  }
}
