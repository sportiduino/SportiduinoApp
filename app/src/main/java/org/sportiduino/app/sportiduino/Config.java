package org.sportiduino.app.sportiduino;

import androidx.annotation.NonNull;

import org.sportiduino.app.App;
import org.sportiduino.app.Password;
import org.sportiduino.app.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public static final int START_STATION  = 240;
    public static final int FINISH_STATION = 245;
    public static final int CHECK_STATION  = 248;
    public static final int CLEAR_STATION  = 249;

    public enum AntennaGain {
        ANTENNA_GAIN_UNKNOWN(App.str(R.string.antenna_gain_unknown), 0),
        ANTENNA_GAIN_18DB("18 " + App.str(R.string.db), 0x02),
        ANTENNA_GAIN_23DB("23 " + App.str(R.string.db), 0x03),
        ANTENNA_GAIN_33DB("33 " + App.str(R.string.db), 0x04),
        ANTENNA_GAIN_38DB("38 " + App.str(R.string.db), 0x05),
        ANTENNA_GAIN_43DB("43 " + App.str(R.string.db), 0x06),
        ANTENNA_GAIN_48DB("48 " + App.str(R.string.db), 0x07);

        private static final Map<Integer, AntennaGain> BY_VALUE = new HashMap<>();

        static {
            for (AntennaGain ag : values()) {
                BY_VALUE.put(ag.value, ag);
            }
        }

        public final String label;
        public final int value;

        AntennaGain(String label, int value) {
            this.label = label;
            this.value = value;
        }

        public static AntennaGain byValue(int value) {
            return BY_VALUE.get(value);
        }

        public static AntennaGain[] realValues() {
            return Arrays.copyOfRange(values(), 1, values().length);
        }

        @NonNull
        @Override
        public String toString() {
            return label;
        }
    }

    public enum ActiveModeDuration {
        ACTIVE_MODE_1H("1 " + App.str(R.string.config_hour), 0),
        ACTIVE_MODE_2H("2 " + App.str(R.string.config_hour), 1),
        ACTIVE_MODE_4H("4 " + App.str(R.string.config_hour), 2),
        ACTIVE_MODE_8H("8 " + App.str(R.string.config_hour), 3),
        ACTIVE_MODE_16H("16 " + App.str(R.string.config_hour), 4),
        ACTIVE_MODE_32H("32 " + App.str(R.string.config_hour), 5),
        ACTIVE_MODE_ALWAYS(App.str(R.string.config_always_active), 6),
        ACTIVE_MODE_NEVER(App.str(R.string.config_never_active), 7);

        private static final Map<Integer, ActiveModeDuration> BY_VALUE = new HashMap<>();

        static {
            for (ActiveModeDuration e : values()) {
                BY_VALUE.put(e.value, e);
            }
        }

        private final String label;
        private final int value;

        ActiveModeDuration(String label, int value) {
            this.label = label;
            this.value = value;
        }

        public static ActiveModeDuration byValue(int value) {
            return BY_VALUE.get(value);
        }

        @NonNull
        @Override
        public String toString() {
            return label;
        }
    }

    public int stationCode = 0;
    public ActiveModeDuration activeModeDuration = ActiveModeDuration.ACTIVE_MODE_2H;
    public boolean checkStartFinish = false;
    public boolean checkCardInitTime = false;
    public boolean autoSleep = false;
    public boolean fastPunch = false;
    public AntennaGain antennaGain = AntennaGain.ANTENNA_GAIN_33DB;
    public Password password = Password.defaultPassword();

    public static Config unpack(byte[] configData) {
        Config config = new Config();
        config.stationCode = configData[0] & 0xFF;

        config.activeModeDuration = ActiveModeDuration.byValue(configData[1] & 0x7);

        config.checkStartFinish = (configData[1] & 0x08) > 0;
        config.checkCardInitTime = (configData[1] & 0x10) > 0;
        config.autoSleep = (configData[1] & 0x20) > 0;
        config.fastPunch = (configData[1] & 0x40) > 0;

        config.antennaGain = AntennaGain.byValue(configData[2] & 0xFF);
        return config;
    }

    public byte[][] pack() {
        //ArrayList<Byte> configData = new ArrayList<>();

        byte flags = (byte) activeModeDuration.value;

        if (checkStartFinish) {
            flags |= 0x08;
        }
        if (checkCardInitTime) {
            flags |= 0x10;
        }
        if (autoSleep) {
            flags |= 0x20;
        }
        if (fastPunch) {
            flags |= 0x40;
        }

        return new byte[][] {
            {(byte) stationCode, flags, (byte) antennaGain.value, (byte) password.getValue(2)},
            {(byte) password.getValue(1), (byte) password.getValue(0), 0, 0}
        };
    }

    @NonNull
    @Override
    public String toString() {
        String str = App.str(R.string.config_station_no_) + stationCode;
        switch (stationCode) {
            case START_STATION:
                str += App.str(R.string.config_start);
                break;
            case FINISH_STATION:
                str += App.str(R.string.config_finish);
                break;
            case CHECK_STATION:
                str += App.str(R.string.config_check);
                break;
            case CLEAR_STATION:
                str += App.str(R.string.config_clear);
                break;
        }
        str += "\n\t" + App.str(R.string.config_active_time) + activeModeDuration.toString();
        str += "\n\t" + App.str(R.string.config_flags);
        if (checkStartFinish) {
            str += "\n\t\t" + App.str(R.string.config_check_start_finish);
        }
        if (checkCardInitTime) {
            str += "\n\t\t" + App.str(R.string.config_check_init_time);
        }
        if (autoSleep) {
            str += "\n\t\t" + App.str(R.string.config_auto_sleep_flag);
        }
        if (fastPunch) {
            str += "\n\t\t" + App.str(R.string.config_fast_punch_flag);
        }
        str += "\n\t" + App.str(R.string.config_antenna_gain_) + antennaGain.label;
        return str;
    }
}

