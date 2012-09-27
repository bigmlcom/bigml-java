package org.bigml.binding;

@SuppressWarnings("serial")
public class InvalidModelException extends Exception {

  /**
   * Forward the default constructor onto the RuntimeException default constructor
   */
  public InvalidModelException() {
    super();
  }

  public InvalidModelException(final Throwable x) {
    super(x);
  }

  /**
   * Forward to the RuntimeException constructor
   */
  public InvalidModelException(final String msg) {
    super(msg);
  }

  /**
   * Forward to the RuntimeException constructor
   */
  public InvalidModelException(final String msg, Throwable t) {
    super(msg, t);
  }
}