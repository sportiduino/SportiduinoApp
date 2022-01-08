package org.sportiduino.app.sportiduino;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private static final int START_STATION  = 240;
    private static final int FINISH_STATION = 245;
    private static final int CHECK_STATION  = 248;
    private static final int CLEAR_STATION  = 249;

    private enum AntennaGain {
        ANTENNA_GAIN_18DB("18 dB", 0x02),
        ANTENNA_GAIN_23DB("23 dB", 0x03),
        ANTENNA_GAIN_33DB("33 dB", 0x04),
        ANTENNA_GAIN_38DB("38 dB", 0x05),
        ANTENNA_GAIN_43DB("43 dB", 0x06),
        ANTENNA_GAIN_48DB("48 dB", 0x07);

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

    @Override
    public String toString() {
        String str = "\tStation No: " + stationCode;
        switch (stationCode) {
            case START_STATION:
                str += " (Start)";
                break;
            case FINISH_STATION:
                str += " (Finish)";
                break;
            case CHECK_STATION:
                str += " (Check)";
                break;
            case CLEAR_STATION:
                str += " (Clear)";
                break;
        }
        String activeModeString = (1 << activeModeDuration) + " h";
        if (activeModeDuration == 64) {
            activeModeString = "always Active";
        } else if (activeModeDuration == 128) {
            activeModeString = "always in Wait";
        }
        str += "\n\tActive time: " + activeModeString;
        str += "\n\tFlags: ";
        if (checkStartFinish) {
            str += "\n\t\tCheck start/finish";
        }
        if (checkCardInitTime) {
            str += "\n\t\tCheck card init time flag";
        }
        if (autoSleep) {
            str += "\n\t\tAutosleep flag";
        }
        if (fastPunch) {
            str += "\n\t\tFast punch flag";
        }
        str += "\n\tAntenna Gain: " + antennaGain.label;
        return str;
    }
}

