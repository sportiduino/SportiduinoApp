package org.sportiduino.app.sportiduino;

public class ReadWriteCardException extends Exception {
    public ReadWriteCardException(String errorMsg) {
        super(errorMsg);
    }

    public ReadWriteCardException() {
        super();
    }
}
