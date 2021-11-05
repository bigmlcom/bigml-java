package org.bigml.binding;

@SuppressWarnings("serial")
public class InputDataParseException extends Exception {

    /**
     * Forward the default constructor onto the RuntimeException default
     * constructor
     */
    public InputDataParseException() {
        super();
    }
    
    /**
     * Forward to the RuntimeException constructor
     * 
     * @param t		the error class to forward the exception
     */
    public InputDataParseException(final Throwable t) {
        super(t);
    }

    /**
     * Forward to the RuntimeException constructor
     * 
     * @param msg	the message for the exception
     */
    public InputDataParseException(final String msg) {
        super(msg);
    }

    /**
     * Forward to the RuntimeException constructor
     * 
     * @param msg	the message for the exception
     * @param t		the error class to forward the exception
     */
    public InputDataParseException(final String msg, Throwable t) {
        super(msg, t);
    }
}
