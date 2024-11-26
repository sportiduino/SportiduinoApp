package org.sportiduino.app;

import android.widget.EditText;

public class ViewUtils {
    public static void setCaretEnd(EditText input) {
        input.setSelection(input.getText().length());
    }

    public static void initCaretEndOnFocus(EditText input) {
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                setCaretEnd(input);
            }
        });
    }
}

