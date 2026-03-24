package org.sportiduino.app;

import static org.sportiduino.app.sportiduino.Config.FINISH_STATION;
import static org.sportiduino.app.sportiduino.Config.START_STATION;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sportiduino.app.Course.CourseType;

public class CourseValidator {
    private Enum<CourseType> type = CourseType.UNKNOWN;

    private final ArrayList<Integer> coursePoints = new ArrayList<>();

    private ArrayList<Integer> cardPointsRaw = new ArrayList<>();
    private ArrayList<Integer> cardPoints = new ArrayList<>();

    private Boolean strictOrder = false;

    private CourseValidatorBase validator;

    public CourseValidator(String value) {
        this.init(value);
    }

    public void init(String value) {
        String[] pairs = value.split(":");

        if (pairs.length > 0) {
            try {
                type = CourseType.valueOf(pairs[0]);
            }  catch (Exception ignored) {}

            if (type == CourseType.ROGAINING) {
                validator = new CourseValidatorRogaining(this);
            }

            if (type == CourseType.ORIENTEERING) {
                validator = new CourseValidatorOrienteering(this);
            }
        }

        if (pairs.length > 1) {
            String[] rawPoints = pairs[1].split(",");

            for (String rawPoint : rawPoints) {
                String point = rawPoint.trim();

                if (!point.isEmpty()) {
                    coursePoints.add(Integer.valueOf(point));
                }
            }
        }

        if (pairs.length > 2) {
            strictOrder = Objects.equals(pairs[2], "1");
        }
    }

    private ArrayList<Integer> parsePoints(String cardData) {
        // 33 - 2025-04-01 10:44:12
        // 42 - 2025-04-01 10:59:40
        // Finish - 2025-04-01 11:41:55

        String regex = "(\\d+|Start|Finish)\\s+?-\\s+?(\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d)";
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

        Matcher matcher = pattern.matcher(cardData);

        ArrayList<Integer> points = new ArrayList<>();

        while (matcher.find()) {
            String point = matcher.group(1);

            if (Objects.equals(point, "Start")) {
                points.add(START_STATION);
                continue;
            }

            if (Objects.equals(point, "Finish")) {
                points.add(FINISH_STATION);
                continue;
            }

            assert point != null;
            points.add(Integer.valueOf(point));
        }

        return points;
    }

    public void parse(String cardData) {
        if (validator == null) {
            return;
        }

        this.cardPointsRaw = this.parsePoints(cardData);
        this.cardPoints = validator.normalizeCardPoints(this.cardPointsRaw);
    }

    public Boolean isValid() {
        return validator.isValid();
    }

    public ArrayList<Integer> getCardPoints() {
        return cardPoints;
    }

    public ArrayList<Integer> getCardPointsRaw() {
        return cardPointsRaw;
    }

    public ArrayList<Integer> getCoursePoints() {
        return coursePoints;
    }

    public ArrayList<Integer> getMissedCoursePoints() {
        return validator.getMissedCoursePoints();
    }

    public Integer getScore() {
        return this.validator.getScore();
    }

    public Enum<CourseType> getType() {
        return type;
    }

    public Boolean isStrictOrder() {
        return strictOrder;
    }
}
