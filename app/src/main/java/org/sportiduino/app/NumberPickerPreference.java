package org.sportiduino.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import android.widget.NumberPicker;

import androidx.preference.DialogPreference;

public class NumberPickerPreference extends DialogPreference {

	private NumberPicker picker;
	private Integer initialValue;
	//private int minValue = 0;  // Default minimum
	//private int maxValue = 10; // Default maximum
	
	public NumberPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.preference_number_picker);

		// Read custom min and max attributes from XML if provided
        //TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference);
        //minValue = a.getInt(R.styleable.NumberPickerPreference_minValue, minValue);
        //maxValue = a.getInt(R.styleable.NumberPickerPreference_maxValue, maxValue);
        //a.recycle();
	}
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		int def = defaultValue instanceof Integer ? (Integer)defaultValue : 0;
		initialValue = restorePersistedValue ? getPersistedInt(def) : def;
        setSummary(initialValue.toString());
	}
		
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 0);
	}

	public int getValue() {
        return initialValue != null ? initialValue : 0;
    }

    public void setValue(int value) {
        initialValue = value;
        persistInt(value);
        setSummary(initialValue.toString());
    }

    //public int getMinValue() {
    //    return minValue;
    //}

    //public int getMaxValue() {
    //    return maxValue;
    //}
}

