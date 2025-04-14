package org.sportiduino.app.sportiduino;

import static org.sportiduino.app.sportiduino.Constants.*;

import android.text.Html;

import org.sportiduino.app.App;
import org.sportiduino.app.Password;
import org.sportiduino.app.R;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MasterCard extends Card {
    final private byte[] password;
    public byte[][] dataForWriting = null;

    public MasterCard(CardAdapter adapter, CardType type, Password password) {
        super(adapter);

        this.type = type;
        this.password = password.toByteArray();
    }

    @Override
    public byte[][] read() throws ReadWriteCardException {
        switch (type) {
            case MASTER_GET_STATE:
                return adapter.readPages(CARD_PAGE_START, 5);
            case MASTER_READ_BACKUP:
                return adapter.readPages(CARD_PAGE_INFO1, adapter.getMaxPage(), true);
            default:
                return adapter.readPages(CARD_PAGE_INFO1, 2);
        }
    }

    @Override
    public CharSequence parseData(byte[][] data) {
        switch (type) {
            case MASTER_SET_TIME:
                return App.str(R.string.time_master_card);
            case MASTER_SET_NUMBER:
                return App.str(R.string.number_master_card);
            case MASTER_GET_STATE:
                State state = new State(data);
                return Html.fromHtml((App.str(R.string.state_master_card) + "\n" + state.toString()).replace("\n", "<br/>"));
            case MASTER_SLEEP:
                return App.str(R.string.sleep_master_card) + "\n" + parseWakeupTime(data);
            case MASTER_READ_BACKUP:
                return parseBackupMaster(data);
            case MASTER_CONFIG:
                return App.str(R.string.config_master_card);
            case MASTER_PASSWORD:
                return App.str(R.string.password_master_card);
            case MASTER_AUTH_PASSWORD:
                return App.str(R.string.auth_password_master_card);
            default:
                return App.str(R.string.unknown_card_type);
        }
    }

    @Override
    protected void writeImpl() throws ReadWriteCardException {
        adapter.disableAuthentication();

        final byte[][] header = {
            {0, (byte) type.value, MASTER_CARD_SIGN, FW_PROTO_VERSION},
            {password[0], password[1], password[2], 0}
        };
        adapter.writePages(CARD_PAGE_INIT, header, header.length);
        if (dataForWriting != null && dataForWriting.length > 0) {
            adapter.writePages(CARD_PAGE_INFO1, dataForWriting, dataForWriting.length);
        }
    }

    public static byte[][] packGetState() {
        // In fact, state data is recorded in 5 pages from index 8 (CARD_PAGE_START),
        // but since only the first byte of CARD_PAGE_START is checked to determine
        // the state of the card as "empty", it is enough to zero out only CARD_PAGE_START
        // and pages with index 6 (CARD_PAGE_INFO1) and 7 (CARD_PAGE_INFO2).
        return new byte[][] {
            {0, 0, 0, 0}, {0, 0, 0, 0},
            {0, 0, 0, 0} // CARD_PAGE_START
        };
    }

    public static byte[][] packStationNumber(int stationNumber) {
        return new byte[][] { {(byte) stationNumber, 0, 0, 0} };
    }

    public static byte[][] packTime(Calendar calendar) {
        Calendar c = (Calendar) calendar.clone();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        int year = c.get(Calendar.YEAR) - 2000;
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        return new byte[][] {
            {(byte) month, (byte) year, (byte) day, 0},
            {(byte) hour, (byte) minute, (byte) second, 0}
        };
    }

    public static byte[][] packNewPassword(Password password) {
        return new byte[][] {
            {(byte) password.getValue(2), (byte) password.getValue(1), (byte) password.getValue(0), 0}
        };
    }

    public static byte[][] packAuthPassword(int[] password) {
        return new byte[][] {
            {(byte) password[3], (byte) password[2], (byte) password[1], (byte) password[0]}
        };
    }

    public static String parseWakeupTime(byte[][] data) {
        int year = data[0][1] + 2000;
        int month = data[0][0];
        int day = data[0][2];
        int hour = data[1][0];
        int minute = data[1][1];
        int second = data[1][2];
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.set(year, month - 1, day, hour, minute, second);
        return App.str(R.string.wakeup_time_) + " " + Util.dhmformat.format(new Date(c.getTimeInMillis()));
    }

    private String parseBackupMaster(byte[][] data) {
        StringBuilder ret = new StringBuilder(App.str(R.string.backup_master_card));
        int stationNumber = dataPage4[0] & 0xff;
        ret.append("\n").append(App.str(R.string.station_no_)).append(stationNumber);
        ret.append("\n").append(App.str(R.string.record_count)).append(" %d");
        int recordCount = 0;
        if (dataPage4[3] == 1) { // old format with timestamps
            long timeHigh12bits = 0;
            long initTime = 0;
            for (byte[] datum : data) {
                if (timeHigh12bits == 0) {
                    initTime = Util.toUint32(datum);
                    timeHigh12bits = initTime & 0xfff00000;
                    continue;
                }

                int cardNum = Util.toUint16(datum[0], datum[1]);
                cardNum >>= 4;

                if (cardNum == 0) {
                    continue;
                }

                long punchTime = Util.toUint32(datum) & 0xfffff | timeHigh12bits;
                if (punchTime < initTime) {
                    punchTime += 0x100000;
                }
                ret.append(String.format("\n%1$4s", cardNum));
                ret.append(" - ").append(Util.dformat.format(new Date(punchTime*1000)));
                ++recordCount;
            }
        } else if (dataPage4[3] >= 10) { // new format (FW version 10 or greater)
            int lastTimeHigh16bits = 0;
            for (byte[] datum : data) {
                int cardNum = Util.toUint16(datum[0], datum[1]);
                int time16bits = Util.toUint16(datum[2], datum[3]);
                if (cardNum == 0) {
                    if (time16bits > 0 && time16bits != lastTimeHigh16bits) {
                        lastTimeHigh16bits = time16bits;
                    }
                    continue;
                }
                long punchTime = (long)lastTimeHigh16bits << 16 | time16bits;
                ret.append(String.format("\n%1$5s", cardNum));
                ret.append(" - ").append(Util.dformat.format(new Date(punchTime*1000)));
                ++recordCount;
            }
        }
        return String.format(ret.toString(), recordCount);
    }
}

