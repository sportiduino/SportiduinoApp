package org.sportiduino.app;

import java.util.Arrays;

public class Password {
    private int[] passwordArray = {0, 0, 0};

    public Password(int[] array) {
        if (array.length > 2) {
            passwordArray = Arrays.copyOf(array, 3);
        }
    }

    public Password(int pass1, int pass2, int pass3) {
        passwordArray = new int[]{pass1, pass2, pass3};
    }

    public static Password defaultPassword() {
        return new Password(0, 0, 0);
    }

    public static Integer parseValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < passwordArray.length; ++i) {
            if (i > 0) {
                str += "-";
            }
            str += String.valueOf(passwordArray[i]);
        }
        return str;
    }

    public byte[] toByteArray() {
        return new byte[] {
            (byte) passwordArray[2],
            (byte) passwordArray[1],
            (byte) passwordArray[0]};
    }

    public static Password fromString(String str) {
        if (str != null) {
            String[] split = str.split("-");
            if (split.length > 2) {
                int[] array = new int[split.length];
                for (int i = 0; i < split.length; i++) {
                    array[i] = Integer.parseInt(split[i]);
                }
                return new Password(array);
            }
        }
        return new Password(0, 0, 0);
    }

    public int getValue(int index) {
        if (0 <= index && index < 3) {
            return passwordArray[index];
        }
        return 0;
    }
}
