package org.sportiduino.app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.util.ArrayList;
import java.util.Objects;

import org.sportiduino.app.Course.CourseType;

public class CardCourseValidationPreferenceDialog extends PreferenceDialogFragmentCompat {
    private static final String SAVE_STATE_TEXT = "CardCourseValidationPreferenceDialogFragment.text";
    private CourseType cardCourseValidationType = CourseType.UNKNOWN;

    private String cardCoursePoints;
    private String cardCoursePointsStrictOrder;

    private ArrayList<RadioButton> radioButtons;

    private LinearLayout courseValidationPoints;
    private TextView courseValidationPointsText;

    private CheckBox courseValidationPointsStrictOrder;

    public static CardCourseValidationPreferenceDialog newInstance(String key) {
        final CardCourseValidationPreferenceDialog
            fragment = new CardCourseValidationPreferenceDialog();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    private CardCourseValidationPreference getCardCourseValidationPreference() {
        return (CardCourseValidationPreference) getPreference();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(SAVE_STATE_TEXT, cardCourseValidationType.toString());
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        CardCourseValidationPreference preference = getCardCourseValidationPreference();

        if (preference.getValue() != null) {
            String[] pairs = preference.getValue().split(":");

            if (pairs.length > 0) {
                cardCourseValidationType = CourseType.valueOf(pairs[0]);
            }

            if (pairs.length > 1) {
                cardCoursePoints = pairs[1];
            }

            if (pairs.length > 2) {
                cardCoursePointsStrictOrder = pairs[2];
            }

            for (int i = 0; i < radioButtons.size(); ++i) {
                if (i == cardCourseValidationType.ordinal()) {
                    radioButtons.get(i).setChecked(true);
                    break;
                }
            }

            processPoints();
        }
    }

    View.OnClickListener radioButtonClickListener = (View view) -> {
        RadioButton radioButton = (RadioButton) view;
        radioButtonChecked(radioButton);

        processPoints();
    };

    private void radioButtonChecked(RadioButton radioButton) {
        int radioButtonId = radioButton.getId();

        if (radioButtonId == R.id.course_validation_type_rogaining) {
            cardCourseValidationType = CourseType.ROGAINING;
        } else if (radioButtonId == R.id.course_validation_type_orienteering) {
            cardCourseValidationType = CourseType.ORIENTEERING;
        } else{
            cardCourseValidationType = CourseType.UNKNOWN;
        }
    }

    private void processPoints() {
        if (cardCourseValidationType == CourseType.ORIENTEERING) {
            courseValidationPoints.setVisibility(View.VISIBLE);
        } else {
            courseValidationPoints.setVisibility(View.GONE);
        }

        courseValidationPointsText.setText(cardCoursePoints);
        courseValidationPointsStrictOrder.setChecked(Objects.equals(cardCoursePointsStrictOrder, "1"));
    }

    @Nullable
    @Override
    protected View onCreateDialogView(@NonNull Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.preference_card_course_validation, null);

        RadioButton checkboxUnknown = view.findViewById(R.id.course_validation_type_unknown);
        RadioButton checkboxOrienteering = view.findViewById(R.id.course_validation_type_orienteering);
        RadioButton checkboxRogaining = view.findViewById(R.id.course_validation_type_rogaining);

        courseValidationPoints = view.findViewById(R.id.course_validation_points);
        courseValidationPointsText = view.findViewById(R.id.course_validation_points_text);
        courseValidationPointsStrictOrder = view.findViewById(R.id.course_validation_points_strict_order);

        radioButtons = new ArrayList<>();

        radioButtons.add(checkboxUnknown);
        radioButtons.add(checkboxOrienteering);
        radioButtons.add(checkboxRogaining);

        for (int i = 0; i < radioButtons.size(); ++i) {
            radioButtons.get(i).setOnClickListener(radioButtonClickListener);
        }

        return view;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String type = cardCourseValidationType.toString();
            String points = courseValidationPointsText.getText().toString();
            String strictOrder = courseValidationPointsStrictOrder.isChecked() ? "1" : "0";

            CardCourseValidationPreference preference = getCardCourseValidationPreference();

            String newValue = type + ":" + points + ":" + strictOrder;

            if (preference.callChangeListener(newValue)) {
                preference.setValue(newValue);
            }
        }
    }
}
