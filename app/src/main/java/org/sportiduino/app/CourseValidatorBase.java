package org.sportiduino.app;

import java.util.ArrayList;

class CourseValidatorBase {
    protected CourseValidator validator;

    public CourseValidatorBase(CourseValidator validator) {
        this.validator = validator;
    }

    public ArrayList<Integer> normalizeCardPoints(ArrayList<Integer> points) {
        return points;
    }

    public Boolean isValid() {
        return false;
    }

    public Integer getScore() {
        return 0;
    }

    public ArrayList<Integer> getMissedCoursePoints() {
        return new ArrayList<>();
    }
}
