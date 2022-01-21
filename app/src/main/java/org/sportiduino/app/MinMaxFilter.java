package org.sportiduino.app;

import android.text.InputFilter;
import android.text.Spanned;

public class MinMaxFilter implements InputFilter {
    private final int min;
    private final int max;
    private int maxLength = -1;

    public MinMaxFilter(int min, int max) {
        this.min = min;
        this.max = max;
        if (min > 0 && max > 0) {
            maxLength = String.valueOf(max).length();
        }
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            String newValue = dest.subSequence(0, dstart)
                    + source.subSequence(start, end).toString()
                    + dest.subSequence(dend, dest.length());
            int input = Integer.parseInt(newValue);
            if (isInRange(min, max, input)) {
                if (maxLength < 0 || newValue.length() <= maxLength) {
                    return null;
                }
            }
        } catch (NumberFormatException ignored) {}
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
