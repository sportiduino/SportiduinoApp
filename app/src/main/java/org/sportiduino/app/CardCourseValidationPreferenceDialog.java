package org.sportiduino.app;

import static org.sportiduino.app.sportiduino.Constants.COLON;
import static org.sportiduino.app.sportiduino.Util.decodeColon;
import static org.sportiduino.app.sportiduino.Util.encodeColon;

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

    private final String delimiter = COLON;

    private String cardCoursePoints;
    private String cardCoursePointsStrictOrder;
    private String cardCourseStartTime;
    private String cardCourseDuration;
    private String cardCoursePenalty;

    private ArrayList<RadioButton> radioButtons;

    private LinearLayout courseValidationPoints;
    private LinearLayout courseValidationTiming;
    private TextView courseValidationPointsText;
    private TextView courseValidationStartTime;
    private TextView courseValidationDuration;
    private TextView courseValidationPenalty;
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
            String[] pairs = preference.getValue().split(delimiter);

            if (pairs.length > 0) {
                cardCourseValidationType = CourseType.valueOf(pairs[0]);
            }

            if (pairs.length > 1) {
                cardCoursePoints = decodeColon(pairs[1]);
            }

            if (pairs.length > 2) {
                cardCoursePointsStrictOrder = pairs[2];
            }

            if (pairs.length > 3) {
                cardCourseStartTime = decodeColon(pairs[3]);
            }

            if (pairs.length > 4) {
                cardCourseDuration = pairs[4];
            }

            if (pairs.length > 5) {
                cardCoursePenalty = pairs[5];
            }

            for (int i = 0; i < radioButtons.size(); ++i) {
                if (i == cardCourseValidationType.ordinal()) {
                    radioButtons.get(i).setChecked(true);
                    break;
                }
            }

            processLayout();
        }
    }

    View.OnClickListener radioButtonClickListener = (View view) -> {
        RadioButton radioButton = (RadioButton) view;
        radioButtonChecked(radioButton);

        processLayout();
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

    private void processLayout() {
        if (cardCourseValidationType == CourseType.ORIENTEERING) {
            courseValidationPoints.setVisibility(View.VISIBLE);
            courseValidationTiming.setVisibility(View.GONE);
        } else if (cardCourseValidationType == CourseType.ROGAINING) {
            courseValidationPoints.setVisibility(View.GONE);
            courseValidationTiming.setVisibility(View.VISIBLE);
        } else {
            courseValidationPoints.setVisibility(View.GONE);
            courseValidationTiming.setVisibility(View.GONE);
        }

        courseValidationPointsText.setText(cardCoursePoints);
        courseValidationPointsStrictOrder.setChecked(Objects.equals(cardCoursePointsStrictOrder, "1"));

        courseValidationStartTime.setText(cardCourseStartTime);
        courseValidationDuration.setText(cardCourseDuration);
        courseValidationPenalty.setText(cardCoursePenalty);
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
        courseValidationTiming = view.findViewById(R.id.course_validation_timing);
        courseValidationStartTime = view.findViewById(R.id.course_validation_start_time);
        courseValidationDuration = view.findViewById(R.id.course_validation_duration);
        courseValidationPenalty = view.findViewById(R.id.course_validation_penalty);

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
            String startTime = courseValidationStartTime.getText().toString();
            String duration = courseValidationDuration.getText().toString();
            String penalty = courseValidationPenalty.getText().toString();

            CardCourseValidationPreference preference = getCardCourseValidationPreference();

            String newValue = type + delimiter + encodeColon(points) + delimiter + strictOrder + delimiter +
                    encodeColon(startTime) + delimiter + duration + delimiter + penalty;

            if (preference.callChangeListener(newValue)) {
                preference.setValue(newValue);
            }
        }
    }
}
