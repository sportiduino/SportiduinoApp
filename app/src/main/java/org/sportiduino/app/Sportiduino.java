package org.sportiduino.app;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import java.util.ArrayList;

public class Sportiduino {

    private static final int START_STATION  = 240;
    private static final int FINISH_STATION = 245;
    private static final int CHECK_STATION  = 248;
    private static final int CLEAR_STATION  = 249;

    private static final int MASTER_CARD_GET_STATE    = 0xF9;
    private static final int MASTER_CARD_SET_TIME     = 0xFA;
    private static final int MASTER_CARD_SET_NUMBER   = 0xFB;
    private static final int MASTER_CARD_SLEEP        = 0xFC;
    private static final int MASTER_CARD_READ_BACKUP  = 0xFD;
    private static final int MASTER_CARD_SET_PASS     = 0xFE;

    private static final int MODE_ACTIVE = 0;
    private static final int MODE_WAIT = 1;
    private static final int MODE_SLEEP = 2;

    private static class Version {
        // Sportiduino version.
        private final int major;
        private final int minor;
        private final int patch;

        Version(int major, int minor, int patch) {
            // Initializes version by bytes from master station.
            // params: major, minor, patch: Bytes from master station.
            if (minor == 0 && patch == 0) {    // old firmwares v1.0 - v2.6
                if (major >= 100 && major <= 104) {   // v1.0 - v1.4
                    this.major = major / 100;
                    this.minor = major % 100;
                    this.patch = 0;
                } else {
                    this.major = (major >> 6) + 1;
                    this.minor = ((major >> 2) & 0x0F) + 1;
                    this.patch = major & 0x03;
                }
                return;
            }

            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        @SuppressLint("DefaultLocale")
        @NonNull
        public String toString() {
            // return: User friendly version string.
            String suffix = String.valueOf(this.patch);
            final int MAX_PATCH_VERSION = 239;
            if (this.patch > MAX_PATCH_VERSION) {
                suffix = String.format("0-beta.%d", (this.patch - MAX_PATCH_VERSION));
            }
            return String.format("v%d.%d.%s", this.major, this.minor, suffix);
        }
    }

    private static class Battery {
        private float voltage;
        private boolean status;

        public Battery(byte batteryByte) {
            if (batteryByte == 0 || batteryByte == 1) {
                // Old firmware
                status = (batteryByte > 0);
            } else {
                voltage = (float) (batteryByte) / 50.f;
                status = (voltage > 3.6f);
            }
        }

        public boolean isOk() {
            return status;
        }

        public float voltage() {
            return voltage;
        }
    }

    private static class Config {
        private static final int ANTENNA_GAIN_18DB = 0x02;
        private static final int ANTENNA_GAIN_23DB = 0x03;
        private static final int ANTENNA_GAIN_33DB = 0x04;
        private static final int ANTENNA_GAIN_38DB = 0x05;
        private static final int ANTENNA_GAIN_43DB = 0x06;
        private static final int ANTENNA_GAIN_48DB = 0x07;

        private int num = 0;
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
            config.num = configData[0];

            config.activeModeDuration = configData[1] & 0x7;

            config.checkStartFinish = (configData[1] & 0x08) > 0;
            config.checkCardInitTime = (configData[1] & 0x10) > 0;
            config.autoSleep = (configData[1] & 0x20) > 0;
            config.fastPunch = (configData[1] & 0x40) > 0;

            config.antennaGain = configData[2];
            return config;
        }

        public Byte[] pack() {
            ArrayList<Byte> configData = new ArrayList<>();
            configData.add((byte) num);

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
            configData.add((byte) antennaGain);
            configData.add((byte) password[0]);
            configData.add((byte) password[1]);
            configData.add((byte) password[2]);

            return (Byte[]) configData.toArray();
        }
    }

    private static class State {
        private Version version = new Version(0, 0, 0);
        private Config config = new Config();
        private int mode = MODE_ACTIVE;
        private Battery battery = new Battery((byte) 0);
        private int timestamp = 0;
    }
}

