package org.sportiduino.app;

import java.util.Arrays;

public abstract class PasswordBase {
    protected int[] passwordArray;

    public PasswordBase(int length) {
        this.passwordArray = new int[length];
    }

    public PasswordBase(int[] array, int expectedLength) {
        this.passwordArray = Arrays.copyOf(array, expectedLength);
    }

    public int getValue(int index) {
        if (0 <= index && index < passwordArray.length) {
            return passwordArray[index];
        }
        return 0;
    }

    public byte[] toByteArray() {
        byte[] array = new byte[passwordArray.length];
        for (int i = 0; i < passwordArray.length; ++i) {
            array[passwordArray.length - 1 - i] = (byte) passwordArray[i];  // reversed order
        }
        return array;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < passwordArray.length; ++i) {
            if (i > 0) {
                sb.append("-");
            }
            sb.append(passwordArray[i]);
        }
        return sb.toString();
    }

    public static Integer parseValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

