package org.opensha.nshmp.exceptions;

/**
 * <p>Title: LocationErrorException</p>
 *
 * <p>Description: This exception is thrown when user does not provide correct
 * inputs for the Interpolation.
 * </p>
 * @author  Ned Field, Nitin Gupta and EV.Leyendecker
 * @version 1.0
 */
public class InterpolationException
    extends Exception {

  /**
   * Constructs a new InterpolationException with null as its detail message.
   * he cause is not initialized, and may subsequently be initialized by a
   * call to Throwable.initCause(java.lang.Throwable).
   */
  public InterpolationException() {
  }

  /**
   * Constructor with String as the error message
   * Constructs a new InterpolationException with the specified detail message.
   * The  cause is not initialized, and may subsequently be initialized by  a
   * call to Throwable.initCause(java.lang.Throwable)
   * @param errMsg : customized error message describing the error, what caused it
   * and what can user do to avoid it.The detail message is saved for later
   * retrieval by the Throwable.getMessage() method.
   */
  public InterpolationException(String errMsg) {
    super(errMsg);
  }
}
