package org.sportiduino.app;

import java.util.ArrayList;

public class CourseValidatorRogaining extends CourseValidatorBase {
    public CourseValidatorRogaining(CourseValidator validator) {
        super(validator);
    }

    public Boolean isValid() {
        return !validator.getCardPoints().isEmpty();
    }

    public Integer getScore() {
        int score = 0;

        ArrayList<Integer> cardPoints = validator.getCardPoints();

        for (int cardPoint : cardPoints) {
            score += (int) Math.floor((double) cardPoint / 10);
        }

        return score;
    }
}
