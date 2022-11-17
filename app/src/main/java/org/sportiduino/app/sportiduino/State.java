package org.sportiduino.app.sportiduino;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import org.sportiduino.app.App;
import org.sportiduino.app.R;

import java.util.Calendar;
import java.util.Date;


public class State {

    enum Mode {
        ACTIVE,
        WAIT,
        SLEEP
    }

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

        public Version(byte major, byte minor, byte patch) {
            this(Util.byteToUint(major), Util.byteToUint(minor), Util.byteToUint(patch));
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
        private final float[][] lipoVoltageGraph = new float[][] {
            {3.25f, 3.35f, 3.45f, 3.55f, 3.75f, 3.95f, 4.10f},
            {0, 5, 20, 40, 60, 95, 100}
        };
        private float voltage = 0;
        private final boolean status;

        private int getChargeLevel(float v) {
            int low = 0;
            final int n = lipoVoltageGraph[0].length - 1;
            int high = n;
            while (low < high) {
                int mid = (low + high)/2;
                if (v > lipoVoltageGraph[0][mid]) {
                    low = mid + 1;
                } else {
                    high = mid;
                }
            }
            if (low > 0 && low < n) {
                float x0 = lipoVoltageGraph[0][low - 1];
                float x1 = lipoVoltageGraph[0][low];
                float y0 = lipoVoltageGraph[1][low - 1];
                float y1 = lipoVoltageGraph[1][low];
                return (int) (y0 + (v - x0)*(y1 - y0)/(x1 - x0));
            }
            return (int) lipoVoltageGraph[1][low];
        }

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

        @NonNull
        @Override
        public String toString() {
            String voltageText = "";
            if (voltage > 0) {
                voltageText = " - " + String.format(App.str(R.string.battery_volts), getChargeLevel(voltage), voltage);
            }

            if (isOk()) {
                return Util.coloredHtmlString(App.str(R.string.battery_ok) + voltageText, "#008000");
            }
            return Util.coloredHtmlString(App.str(R.string.battery_low) + voltageText, "red");
        }
    }

    Version version;
    Config config;
    Mode mode;
    Battery battery;
    long timestamp;
    long wakeupTimestamp;
    boolean isEmpty = false;

    public State(byte[][] data) {
        try {
            if (data == null || data.length == 0 || data[0][0] == 0) {
                isEmpty = true;
                return;
            }
            version = new Version(data[0][0], data[0][1], data[0][2]);
            config = Config.unpack(data[1]);
            battery = new Battery(data[2][0] & 0xFF);
            mode = Mode.values()[data[2][1]];
            timestamp = Util.toUint32(data[3]);
            wakeupTimestamp = Util.toUint32(data[4]);
        } catch (ArrayIndexOutOfBoundsException e) {
            isEmpty = true;
        }
    }

    @NonNull
    @Override
    public String toString() {
        if (isEmpty) {
            return "Empty";
        }
        String stringState = App.str(R.string.version_) + " " + version.toString();
        stringState += App.str(R.string.state_config_) + config.toString();
        stringState += App.str(R.string.state_battery_) + " " + battery.toString();
        stringState += App.str(R.string.state_mode_) + " " + Util.capitalize(mode.name());
        String clockStr = Util.dformat.format(new Date(timestamp*1000));
        long nowSec = Calendar.getInstance().getTimeInMillis()/1000;
        if (timestamp < (nowSec - 60) || timestamp > (nowSec + 60)) {
            clockStr = Util.coloredHtmlString(clockStr, "red");
        }
        stringState += App.str(R.string.state_clock_) + " " + clockStr;
        stringState += App.str(R.string.state_alarm_) + " " + Util.dformat.format(new Date(wakeupTimestamp*1000));
        return stringState;
    }
}

