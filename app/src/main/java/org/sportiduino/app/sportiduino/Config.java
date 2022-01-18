package org.sportiduino.app.sportiduino;

import androidx.annotation.NonNull;

import org.sportiduino.app.App;
import org.sportiduino.app.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public static final int START_STATION  = 240;
    public static final int FINISH_STATION = 245;
    public static final int CHECK_STATION  = 248;
    public static final int CLEAR_STATION  = 249;

    private enum AntennaGain {
        ANTENNA_GAIN_UNKNOWN("unknown", 0),
        ANTENNA_GAIN_18DB("18 " + "dB", 0x02),
        ANTENNA_GAIN_23DB("23 " + "dB", 0x03),
        ANTENNA_GAIN_33DB("33 " + "dB", 0x04),
        ANTENNA_GAIN_38DB("38 " + "dB", 0x05),
        ANTENNA_GAIN_43DB("43 " + "dB", 0x06),
        ANTENNA_GAIN_48DB("48 " + "dB", 0x07);

        private static final Map<Integer, AntennaGain> BY_VALUE = new HashMap<>();

        static {
            for (AntennaGain ag : values()) {
                BY_VALUE.put(ag.value, ag);
            }
        }

        public final String label;
        public final int value;

        private AntennaGain(String label, int value) {
            this.label = label;
            this.value = value;
        }

        public static AntennaGain byValue(int value) {
            return BY_VALUE.get(value);
        }
    }

    private int stationCode = 0;
    private int activeModeDuration = 1;
    private boolean checkStartFinish = false;
    private boolean checkCardInitTime = false;
    private boolean autoSleep = false;
    private boolean fastPunch = false;
    private AntennaGain antennaGain = AntennaGain.ANTENNA_GAIN_33DB;
    private int[] password;

    public Config() {
        password = new int[]{0, 0, 0};
    }

    public static Config unpack(byte[] configData) {
        Config config = new Config();
        config.stationCode = configData[0] & 0xFF;

        config.activeModeDuration = configData[1] & 0x7;

        config.checkStartFinish = (configData[1] & 0x08) > 0;
        config.checkCardInitTime = (configData[1] & 0x10) > 0;
        config.autoSleep = (configData[1] & 0x20) > 0;
        config.fastPunch = (configData[1] & 0x40) > 0;

        config.antennaGain = AntennaGain.byValue(configData[2] & 0xFF);
        return config;
    }

    public Byte[] pack() {
        ArrayList<Byte> configData = new ArrayList<>();
        configData.add((byte) stationCode);

        byte flags = (byte) activeModeDuration;

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
        configData.add(flags);
        configData.add((byte) antennaGain.value);
        configData.add((byte) password[0]);
        configData.add((byte) password[1]);
        configData.add((byte) password[2]);

        return (Byte[]) configData.toArray();
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
        String activeModeString = (1 << activeModeDuration) + App.str(R.string.config_hour);
        if (activeModeDuration == 64) {
            activeModeString = App.str(R.string.config_always_active);
        } else if (activeModeDuration == 128) {
            activeModeString = App.str(R.string.config_always_in_wait);
        }
        str += App.str(R.string.config_active_time) + activeModeString;
        str += App.str(R.string.config_flags);
        if (checkStartFinish) {
            str += App.str(R.string.config_check_start_finish);
        }
        if (checkCardInitTime) {
            str += App.str(R.string.config_check_init_time);
        }
        if (autoSleep) {
            str += App.str(R.string.config_auto_sleep_flag);
        }
        if (fastPunch) {
            str += App.str(R.string.config_fast_punch_flag);
        }
        str += App.str(R.string.config_antenna_gain_) + antennaGain.label;
        return str;
    }
}

