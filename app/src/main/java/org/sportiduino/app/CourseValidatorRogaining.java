package org.sportiduino.app;

import org.sportiduino.app.sportiduino.Config;

import java.util.ArrayList;
import java.util.List;

public class CourseValidatorRogaining extends CourseValidatorBase {
    public CourseValidatorRogaining(CourseValidator validator) {
        super(validator);
    }

    public Boolean isValid() {
        boolean hasStartPoint = false;
        boolean hasFinishPoint = false;

        ArrayList<Integer> cardPoints = validator.getCardPoints();

        // check start and finish first
        for (int cardPoint : cardPoints) {
            if (cardPoint == Config.START_STATION) {
                hasStartPoint = true;
                continue;
            }

            if (cardPoint == Config.FINISH_STATION) {
                hasFinishPoint = true;
            }
        }

        if (!hasStartPoint || !hasFinishPoint) {
            return false;
        }

        return !validator.getCardPoints().isEmpty();
    }

    public ArrayList<Integer> getMissedCoursePoints() {
        ArrayList<Integer> coursePoints =
                new ArrayList<>(List.of(Config.START_STATION, Config.FINISH_STATION));
        ArrayList<Integer> cardPoints = validator.getCardPoints();

        ArrayList<Integer> missedCoursePoints = new ArrayList<>(coursePoints);
        missedCoursePoints.removeAll(cardPoints);

        return missedCoursePoints;
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
