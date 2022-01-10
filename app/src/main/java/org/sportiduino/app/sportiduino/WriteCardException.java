package org.sportiduino.app.sportiduino;

public class WriteCardException extends Exception {
    public WriteCardException(String errorMsg) {
        super(errorMsg);
    }

    public WriteCardException() {
        super();
    }
}
