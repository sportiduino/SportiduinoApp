package org.sportiduino.app;

import org.sportiduino.app.sportiduino.Config;

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
            // ignore start and finish points
            if (cardPoint == Config.START_STATION || cardPoint == Config.FINISH_STATION) {
                continue;
            }

            score += (int) Math.floor((double) cardPoint / 10);
        }

        return score;
    }
}
