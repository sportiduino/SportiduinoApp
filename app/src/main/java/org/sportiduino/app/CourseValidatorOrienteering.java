package org.sportiduino.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class CourseValidatorOrienteering extends CourseValidatorBase {
    public CourseValidatorOrienteering(CourseValidator courseValidator) {
        super(courseValidator);
    }

    public ArrayList<Integer> normalizeCardPoints(ArrayList<Integer> points) {
        ArrayList<Integer> coursePoints = this.validator.getCoursePoints();
        ArrayList<Integer> normalizedPoints = new ArrayList<>();

        for (Integer point : points) {
            for (Integer coursePoint : coursePoints) {
                if (Objects.equals(point, coursePoint)) {
                    normalizedPoints.add(point);
                    break;
                }
            }
        }

        return normalizedPoints;
    }

    public ArrayList<Integer> getMissedCoursePoints() {
        ArrayList<Integer> coursePoints = this.validator.getCoursePoints();
        ArrayList<Integer> cardPoints = this.validator.getCardPoints();

        ArrayList<Integer> missedCoursePoints = new ArrayList<>(coursePoints);
        missedCoursePoints.removeAll(cardPoints);

        return missedCoursePoints;
    }

    private Boolean isValidPoints() {
        ArrayList<Integer> coursePoints = this.validator.getCoursePoints();
        ArrayList<Integer> cardPoints = this.validator.getCardPoints();

        if (!validator.isStrictOrder()) {
            Collections.sort(coursePoints);
            Collections.sort(cardPoints);
        }

        int i = 0;

        for (int j = 0; j < cardPoints.size() && i < coursePoints.size(); j++) {
            if (cardPoints.get(j).equals(coursePoints.get(i))) {
                i++;
            }
        }

        return i == coursePoints.size();
    }

    public Boolean isValid() {
        return this.isValidPoints();
    }

    public Integer getScore() {
        return this.validator.getCardPoints().size();
    }
}
