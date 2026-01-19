package org.sportiduino.app;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import org.sportiduino.app.Course.CourseType;

public class CardCourseValidationPreference extends DialogPreference {
    public CardCourseValidationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setSummary(getPlaceholderValue());
    }

    private String getPlaceholderValue() {
        return "(" + App.str(R.string.not_specified) + ")";
    }

    private String getValueOrPlaceholderValue() {
        String value = getValue();

        if (value != null) {
            String[] pairs = value.split(":");

            String typeValue = pairs[0];

            if (CourseType.valueOf(typeValue) == CourseType.UNKNOWN) {
                return getPlaceholderValue();
            }

            return typeValue;
        }

        return null;
    }

    public String getValue() {
        return getPersistedString(null);
    }

    public void setValue(String value) {
        persistString(value);

        setSummary(getValueOrPlaceholderValue());

        notifyChanged();
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.preference_card_course_validation;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        super.onSetInitialValue(defaultValue);

        setSummary(getValueOrPlaceholderValue());
    }
}

