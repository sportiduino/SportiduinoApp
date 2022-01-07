package org.sportiduino.app.sportiduino;

import java.util.ArrayList;

public class Config {
    private static final int START_STATION  = 240;
    private static final int FINISH_STATION = 245;
    private static final int CHECK_STATION  = 248;
    private static final int CLEAR_STATION  = 249;

    private static final int ANTENNA_GAIN_18DB = 0x02;
    private static final int ANTENNA_GAIN_23DB = 0x03;
    private static final int ANTENNA_GAIN_33DB = 0x04;
    private static final int ANTENNA_GAIN_38DB = 0x05;
    private static final int ANTENNA_GAIN_43DB = 0x06;
    private static final int ANTENNA_GAIN_48DB = 0x07;

    private int stationCode = 0;
    private int activeModeDuration = 2;  // hours
    private boolean checkStartFinish = false;
    private boolean checkCardInitTime = false;
    private boolean autoSleep = false;
    private boolean fastPunch = false;
    private int antennaGain = ANTENNA_GAIN_33DB;
    private int[] password;

    public Config() {
        password = new int[]{0, 0, 0};
    }

    public static Config unpack(byte[] configData) {
        Config config = new Config();
        config.stationCode = configData[0] & 0xFF;

        int activeModeBits = configData[1] & 0x7;
        config.activeModeDuration = 1 << activeModeBits;

        config.checkStartFinish = (configData[1] & 0x08) > 0;
        config.checkCardInitTime = (configData[1] & 0x10) > 0;
        config.autoSleep = (configData[1] & 0x20) > 0;
        config.fastPunch = (configData[1] & 0x40) > 0;

        config.antennaGain = configData[2];
        return config;
    }

    public Byte[] pack() {
        ArrayList<Byte> configData = new ArrayList<>();
        configData.add((byte) stationCode);

        byte flags = (byte) activeModeDuration; // FIXME

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
        configData.add((byte) antennaGain);
        configData.add((byte) password[0]);
        configData.add((byte) password[1]);
        configData.add((byte) password[2]);

        return (Byte[]) configData.toArray();
    }

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
        String activeModeString = activeModeDuration + " h";
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
        str += "\n\tAntenna Gain: " + antennaGain;
        return str;
    }
}

