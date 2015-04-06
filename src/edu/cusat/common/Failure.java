package edu.cusat.common;

public class Failure extends Exception {

    private static final long serialVersionUID = -8596146907005492757L;

    public Failure() {
    }

    public Failure(String message) {
        super(message);
    }

    public Failure(Throwable cause) {
        super(cause);
    }

    public Failure(String message, Throwable cause) {
        super(message, cause);
    }
}
