package org.opensha.exceptions;

/**
 * <p>Title: LocationOutOfRegionBoundsException</p>
 *
 * <p>Description: This exception is thrown when the user throws the
 * region constraint exception.If MinLat is greater then MaxLat , similarly
 * if MinLon is greater then MaxLon then this exception is thrown.</p>
 * @author Ned Field, Nitin Gupta
 * @version 1.0
 */
public class LocationOutOfRegionBoundsException extends RuntimeException{

    /**
     * Constructs a new LocationOutOfRegionBoundsException with null as its detail message.
     * he cause is not initialized, and may subsequently be initialized by a
     * call to Throwable.initCause(java.lang.Throwable).
     */
    public LocationOutOfRegionBoundsException() {
    }

    /**
     * Constructor with String as the error message
     * Constructs a new LocationOutOfRegionBoundsException with the specified detail message.
     * The  cause is not initialized, and may subsequently be initialized by  a
     * call to Throwable.initCause(java.lang.Throwable)
     * @param errMsg : customized error message describing the error, what caused it
     * and what can user do to avoid it.The detail message is saved for later
     * retrieval by the Throwable.getMessage() method.
     */
    public LocationOutOfRegionBoundsException(String errMsg) {
      super(errMsg);
  }
}
