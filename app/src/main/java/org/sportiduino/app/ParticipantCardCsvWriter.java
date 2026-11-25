package org.sportiduino.app;

import android.content.Context;
import android.os.Environment;

import org.sportiduino.app.sportiduino.Config;
import org.sportiduino.app.sportiduino.ParticipantCard;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParticipantCardCsvWriter {

    private static final String FILE_PREFIX = "cards";
    private static final String FILE_EXTENSION = ".csv";
    private static final char CSV_SEPARATOR = ';';

    private static final String HEADER =
            String.join(String.valueOf(CSV_SEPARATOR),
                    "read_time",
                    "card_number",
                    "fast_punch",
                    "clear_time",
                    "record_count",
                    "punches");

    private final File directory;

    private final SimpleDateFormat fileDateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private final SimpleDateFormat dateTimeFormat =
            new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.US);

    public ParticipantCardCsvWriter(Context context) {
        File baseDir = context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS);

        if (baseDir == null) {
            baseDir = context.getFilesDir();
        }

        //directory = new File(baseDir, "SportiduinoApp");
        directory = baseDir;
    }

    public synchronized void save(ParticipantCard.Data data)
            throws IOException {

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Cannot create directory: " + directory);
        }

        Date now = new Date();

        String fileName = FILE_PREFIX
                + fileDateFormat.format(now)
                + FILE_EXTENSION;

        File file = new File(directory, fileName);

        boolean writeHeader = !file.exists() || file.length() == 0;

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file, true),
                        StandardCharsets.UTF_8))) {

            if (writeHeader) {
                writer.write(HEADER);
                writer.newLine();
            }

            StringBuilder row = new StringBuilder();

            appendField(row, formatDate(now));
            appendField(row, String.valueOf(data.cardNumber));
            appendField(row, String.valueOf(data.fastPunch));
            appendField(row, formatDate(
                    new Date(data.clearTimestamp * 1000)));
            appendField(row, String.valueOf(data.punches.size()));
            appendPunches(row, data.punches);

            writer.write(row.toString());
            writer.newLine();
        }
    }

    private void appendField(StringBuilder sb, String value) {
        if (sb.length() > 0) {
            sb.append(CSV_SEPARATOR);
        }
        sb.append(csvEscape(value));
    }

    private String formatDate(Date date) {
        return dateTimeFormat.format(date);
    }

    private String getControlPointName(int cp) {
        switch (cp) {
            case Config.START_STATION:
                return App.str(R.string.start);

            case Config.FINISH_STATION:
                return App.str(R.string.finish);

            default:
                return String.valueOf(cp);
        }
    }

    private void appendPunches(StringBuilder sb, List<ParticipantCard.Data.Punch> punches) {
        for (ParticipantCard.Data.Punch punch : punches) {
            appendField(sb, getControlPointName(punch.controlPoint));
            appendField(sb, formatDate(new Date(punch.timestamp * 1000)));
        }
    }

    private String csvEscape(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
