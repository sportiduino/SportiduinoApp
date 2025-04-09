package org.sportiduino.app;

public class Password extends PasswordBase {
    public Password(int pass1, int pass2, int pass3) {
        super(3);
        passwordArray[0] = pass1;
        passwordArray[1] = pass2;
        passwordArray[2] = pass3;
    }

    public Password(int[] array) {
        super(array, 3);
    }

    public static Password defaultPassword() {
        return new Password(0, 0, 0);
    }

    public static Password fromString(String str) {
        if (str != null) {
            String[] split = str.split("-");
            if (split.length >= 3) {
                int[] array = new int[3];
                for (int i = 0; i < 3; i++) {
                    array[i] = PasswordBase.parseValue(split[i]);
                }
                return new Password(array);
            }
        }
        return defaultPassword();
    }
}

