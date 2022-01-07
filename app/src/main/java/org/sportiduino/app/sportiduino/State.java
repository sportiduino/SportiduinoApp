package org.sportiduino.app.sportiduino;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;


//private static final int MASTER_CARD_GET_STATE    = 0xF9;
//private static final int MASTER_CARD_SET_TIME     = 0xFA;
//private static final int MASTER_CARD_SET_NUMBER   = 0xFB;
//private static final int MASTER_CARD_SLEEP        = 0xFC;
//private static final int MASTER_CARD_READ_BACKUP  = 0xFD;
//private static final int MASTER_CARD_SET_PASS     = 0xFE;


public class State {

    private static final int MODE_ACTIVE = 0;
    private static final int MODE_WAIT = 1;
    private static final int MODE_SLEEP = 2;

    public static class Version {
        // Sportiduino version.
        private final int major;
        private final int minor;
        private final int patch;

        public Version(int major, int minor, int patch) {
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

    public static class Battery {
        private float voltage = 0;
        private boolean status;

        public Battery(int batteryByte) {
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

        public String toString() {
            String voltageText = "";
            if (voltage > 0) {
                voltageText = String.format(" (%.2f V)", voltage);
            }

            if (isOk()) {
                return "OK" + voltageText;
            }
            return "Low" + voltageText;
        }
    }

    Version version;
    Config config;
    int mode = MODE_ACTIVE;
    Battery battery;
    int timestamp = 0;

    public State(byte[][] data) {
        version = new Version(data[0][0], data[0][1], data[0][2]);
        config = Config.unpack(data[1]);
        battery = new Battery(data[2][0] & 0xFF);
        mode = data[2][1];
    }

    public String toString() {
        String stringState = "Version: " + version.toString();
        stringState += "\nConfig:\n" + config.toString();
        stringState += "\nBattery: " + battery.toString();
        return stringState;
    }
}

