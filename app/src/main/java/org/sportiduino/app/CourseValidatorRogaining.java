package org.sportiduino.app;

import static org.sportiduino.app.sportiduino.Config.FINISH_STATION;
import static org.sportiduino.app.sportiduino.Config.START_STATION;

import java.util.ArrayList;
import java.util.Objects;

public class CourseValidatorRogaining extends CourseValidatorBase {
    public CourseValidatorRogaining(CourseValidator validator) {
        super(validator);
    }

    public ArrayList<Integer> normalizeCardPoints(ArrayList<Integer> points) {
        ArrayList<Integer> normalizedPoints = new ArrayList<>();

        for (Integer point : points) {
            if (Objects.equals(point, START_STATION) || Objects.equals(point, FINISH_STATION)) {
                continue;
            }

            normalizedPoints.add(point);
        }

        return normalizedPoints;
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
