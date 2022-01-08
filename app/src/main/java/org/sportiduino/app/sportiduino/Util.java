package org.sportiduino.app.sportiduino;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class Util {
    public static SimpleDateFormat dformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static int byteToUint(byte b) {
        return ((int) b) & 0xFF;
    }

    public static long toUint32(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8).put(new byte[]{0,0,0,0}).put(Arrays.copyOfRange(bytes, 0, 4));
        buffer.position(0);
        return buffer.getLong();
    }
 
    public static byte[] fromUint32(long value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putLong(value);
        return Arrays.copyOfRange(bytes, 4, 8);
    }

    public static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}

