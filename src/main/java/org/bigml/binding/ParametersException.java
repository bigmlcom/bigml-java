package org.bigml.binding;

@SuppressWarnings("serial")
public class ParametersException extends Exception {

    /**
     * Forward the default constructor onto the RuntimeException default
     * constructor
     */
    public ParametersException() {
        super();
    }

    public ParametersException(final Throwable x) {
        super(x);
    }

    /**
     * Forward to the RuntimeException constructor
     */
    public ParametersException(final String msg) {
        super(msg);
    }

    /**
     * Forward to the RuntimeException constructor
     */
    public ParametersException(final String msg, Throwable t) {
        super(msg, t);
    }
}
