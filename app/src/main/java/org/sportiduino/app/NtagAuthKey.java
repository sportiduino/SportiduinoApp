package org.sportiduino.app;

import java.util.Arrays;

public class NtagAuthKey extends PasswordBase {

    public NtagAuthKey(int k1, int k2, int k3, int k4) {
        super(4);
        passwordArray[0] = k1;
        passwordArray[1] = k2;
        passwordArray[2] = k3;
        passwordArray[3] = k4;
    }

    public NtagAuthKey(int[] array) {
        super(array, 4);
    }

    public static NtagAuthKey defaultKey() {
        return new NtagAuthKey(255, 255, 255, 255);
    }

    public boolean isDefault() {
        return Arrays.equals(passwordArray, defaultKey().passwordArray);
    }

    public static NtagAuthKey fromString(String str) {
        if (str != null) {
            String[] split = str.split("-");
            if (split.length >= 4) {
                int[] array = new int[4];
                for (int i = 0; i < 4; i++) {
                    array[i] = PasswordBase.parseValue(split[i]);
                }
                return new NtagAuthKey(array);
            }
        }
        return defaultKey();
    }
}
