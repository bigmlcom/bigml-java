package org.bigml.binding;

@SuppressWarnings("serial")
public class InputDataParseException extends Exception {

  /**
   * Forward the default constructor onto the RuntimeException default constructor
   */
  public InputDataParseException() {
    super();
  }

  public InputDataParseException(final Throwable x) {
    super(x);
  }

  /**
   * Forward to the RuntimeException constructor
   */
  public InputDataParseException(final String msg) {
    super(msg);
  }

  /**
   * Forward to the RuntimeException constructor
   */
  public InputDataParseException(final String msg, Throwable t) {
    super(msg, t);
  }
}
