package org.sportiduino.app;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class Sportiduino {
    private static final int START_BYTE = 0xFE;

    private static final int START_STATION  = 240;
    private static final int FINISH_STATION = 245;
    private static final int CHECK_STATION  = 248;
    private static final int CLEAR_STATION  = 249;

    // Protocol commands
    private static final int CMD_INIT_TIME_CARD     = 0x41;
    private static final int CMD_INIT_CP_NUM_CARD   = 0x42;
    private static final int CMD_INIT_PASSWORD_CARD = 0x43;  // deprecated
    private static final int CMD_INIT_CARD          = 0x44;
    private static final int CMD_WRITE_PAGES6_7     = 0x45;
    private static final int CMD_READ_VERSION       = 0x46;
    private static final int CMD_INIT_BACKUP_READER = 0x47;
    private static final int CMD_READ_BACKUP_READER = 0x48;
    private static final int CMD_SET_READ_MODE     = 0x49;  // deprecated
    private static final int CMD_WRITE_SETTINGS    = 0x4a;
    private static final int CMD_READ_CARD         = 0x4b;
    private static final int CMD_READ_RAW          = 0x4c;
    private static final int CMD_READ_SETTINGS     = 0x4d;
    private static final int CMD_INIT_SLEEP_CARD   = 0x4e;
    private static final int CMD_APPLY_PWD         = 0x4f;
    private static final int CMD_INIT_STATE_CARD   = 0x50;
    private static final int CMD_READ_CARD_TYPE    = 0x51;
    private static final int CMD_BEEP_ERROR        = 0x58;
    private static final int CMD_BEEP_OK           = 0x59;
    private static final int CMD_INIT_CONFIG_CARD  = 0x5a;

    // Protocol responses
    private static final int RESP_BACKUP         = 0x61;
    private static final int RESP_CARD_DATA      = 0x63;
    private static final int RESP_CARD_RAW       = 0x65;
    private static final int RESP_VERSION        = 0x66;
    private static final int RESP_SETTINGS       = 0x67;
    private static final int RESP_MODE           = 0x69;  // deprecated
    private static final int RESP_CARD_TYPE      = 0x70;
    private static final int RESP_ERROR          = 0x78;
    private static final int RESP_OK             = 0x79;

    // Protocol error codes
    private static final int ERR_COM             = 0x01;
    private static final int ERR_WRITE_CARD      = 0x02;
    private static final int ERR_READ_CARD       = 0x03;
    private static final int ERR_READ_EEPROM     = 0x04;
    private static final int ERR_CARD_NOT_FOUND  = 0x05;
    private static final int ERR_UNKNOWN_CMD     = 0x06;

    private static final int MASTER_CARD_GET_STATE    = 0xF9;
    private static final int MASTER_CARD_SET_TIME     = 0xFA;
    private static final int MASTER_CARD_SET_NUMBER   = 0xFB;
    private static final int MASTER_CARD_SLEEP        = 0xFC;
    private static final int MASTER_CARD_READ_BACKUP  = 0xFD;
    private static final int MASTER_CARD_SET_PASS     = 0xFE;

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
}

