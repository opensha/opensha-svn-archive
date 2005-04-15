package org.opensha.nshmp.exceptions;

/**
 * <p>Title: AnalysisOptionNotSupportedException</p>
 *
 * <p>Description: This exception is thrown when user provide incorrect
 * Analysis Option.</p>
 * @author  Ned Field, Nitin Gupta and EV.Leyendecker
 * @version 1.0
 */
public class AnalysisOptionNotSupportedException
    extends Exception {

  /**
   * Constructs a new AnalysisOptionNotSupportedException with null as its detail message.
   * he cause is not initialized, and may subsequently be initialized by a
   * call to Throwable.initCause(java.lang.Throwable).
   */
  public AnalysisOptionNotSupportedException() {
  }

  /**
   * Constructor with String as the error message
   * Constructs a new AnalysisOptionNotSupportedException with the specified detail message.
   * The  cause is not initialized, and may subsequently be initialized by  a
   * call to Throwable.initCause(java.lang.Throwable)
   * @param errMsg : customized error message describing the error, what caused it
   * and what can user do to avoid it.The detail message is saved for later
   * retrieval by the Throwable.getMessage() method.
   */
  public AnalysisOptionNotSupportedException(String errMsg) {
    super(errMsg);
  }
}
