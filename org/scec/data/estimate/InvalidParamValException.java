package org.scec.data.estimate;

/**
 * <p>Title: InvalidParamValException.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class InvalidParamValException extends RuntimeException {

  public InvalidParamValException() {
  }

  public InvalidParamValException(String message) {
    super(message);
  }

  public InvalidParamValException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidParamValException(Throwable cause) {
    super(cause);
  }
}