package org.sportiduino.app.sportiduino;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;

import android.view.View;
import androidx.core.content.ContextCompat;
import org.sportiduino.app.App;
import org.sportiduino.app.R;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import static org.sportiduino.app.sportiduino.Constants.OPERATED_YEAR_MIN;

public class Util {
    public static SimpleDateFormat dformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat dhmformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static int byteToUint(byte b) {
        return ((int) b) & 0xFF;
    }

    public static long toUint32(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8).put(new byte[]{0,0,0,0}).put(Arrays.copyOfRange(bytes, 0, 4));
        buffer.position(0);
        return buffer.getLong();
    }

    public static int toUint16(byte b1, byte b2) {
        int ret = (b1 & 0xff) << 8;
        ret |= b2 & 0xff;
        return ret;
    }
 
    public static byte[] fromUint32(long value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putLong(value);
        return Arrays.copyOfRange(bytes, 4, 8);
    }

    public static byte[] fromUint16(int value) {
        byte[] bytes = new byte[4];
        ByteBuffer.wrap(bytes).putInt(value);
        return Arrays.copyOfRange(bytes, 2, 4);
    }

    public static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public interface Callback {
        void call(CharSequence str);
    }

    public static SpannableString colorString(String s, int bgColor) {
        SpannableString spannableString = new SpannableString(s);
        spannableString.setSpan(new BackgroundColorSpan(bgColor), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public static SpannableString error(String s) {
        return colorString(s, App.color(R.color.error_text_bg));
    }

    public static SpannableString error(String s, View view) {
        int color = ContextCompat.getColor(view.getContext(), R.color.error_text_bg);

        return colorString(s, color);
    }

    public static SpannableString ok(String s) {
        return colorString(s, App.color(R.color.ok_text_bg));
    }

    public static SpannableString ok(String s, View view) {
        int color = ContextCompat.getColor(view.getContext(), R.color.ok_text_bg);

        return colorString(s, color);
    }

    public static String coloredHtmlString(String s, String color) {
        return String.format("<font color=\"%s\">%s</font>", color, s);
    }

    public static String colorToHexCode(Integer color) {
        return String.format("#%06x", App.color(color) & 0x00ffffff);
    }

    public static Boolean checkOperatedYearMin() {
        Calendar calendar = Calendar.getInstance();

        return calendar.get(Calendar.YEAR) >= OPERATED_YEAR_MIN;
    }
}

