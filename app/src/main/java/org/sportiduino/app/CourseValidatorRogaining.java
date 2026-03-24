package org.sportiduino.app;

import org.sportiduino.app.sportiduino.Config;

import static org.sportiduino.app.sportiduino.Config.FINISH_STATION;
import static org.sportiduino.app.sportiduino.Config.START_STATION;

import java.util.ArrayList;
import java.util.List;
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
