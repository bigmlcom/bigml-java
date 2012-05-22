package org.bigml.binding;

@SuppressWarnings("serial")
public class AuthenticationException extends Exception {

  /**
   * Forward the default constructor onto the RuntimeException default constructor
   */
  public AuthenticationException() {
    super();
  }

  public AuthenticationException(final Throwable x) {
    super(x);
  }

  /**
   * Forward to the RuntimeException constructor
   */
  public AuthenticationException(final String msg) {
    super(msg);
  }

  /**
   * Forward to the RuntimeException constructor
   */
  public AuthenticationException(final String msg, Throwable t) {
    super(msg, t);
  }
}
