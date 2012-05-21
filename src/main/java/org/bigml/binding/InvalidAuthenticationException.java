package org.bigml.binding;

@SuppressWarnings("serial")
public class InvalidAuthenticationException extends Exception {
	
  /**
   * Forward the default constructor onto the RuntimeException default constructor
   */
  public InvalidAuthenticationException() {
    super();
  }

  public InvalidAuthenticationException(final Throwable x) {
    super(x);
  }

  /**
   * Forward to the RuntimeException constructor
   */
  public InvalidAuthenticationException(final String msg) {
    super(msg);
  }

  /**
   * Forward to the RuntimeException constructor
   */
  public InvalidAuthenticationException(final String msg, Throwable t) {
    super(msg, t);
  }
}
