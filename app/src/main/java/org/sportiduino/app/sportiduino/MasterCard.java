package org.sportiduino.app.sportiduino;

import static org.sportiduino.app.sportiduino.Constants.*;

import android.text.Html;
import android.util.Log;

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
                return adapter.readPages(8, 12);
            case MASTER_READ_BACKUP:
                return adapter.readPages(CARD_PAGE_INFO1, adapter.getMaxPage(), true);
            default:
                return new byte[0][0];
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
                return App.str(R.string.sleep_master_card);
            case MASTER_READ_BACKUP:
                return parseBackupMaster(data);
            case MASTER_CONFIG:
                return App.str(R.string.config_master_card);
            case MASTER_PASSWORD:
                return App.str(R.string.password_master_card);
            default:
                return App.str(R.string.unknown_card_type);
        }
    }

    @Override
    protected void writeImpl() throws ReadWriteCardException {
        final byte[][] header = {
                {0, (byte) type.value, MASTER_CARD_SIGN, FW_PROTO_VERSION},
                {password[0], password[1], password[2], 0}
        };
        adapter.writePages(CARD_PAGE_INIT, header, header.length);
        if (dataForWriting != null && dataForWriting.length > 0) {
            adapter.writePages(6, dataForWriting, dataForWriting.length);
        }
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

    public static byte[][] packStationNumber(int stationNumber) {
        return new byte[][] { {(byte) stationNumber, 0, 0, 0} };
    }

    public static byte[][] packTime(Calendar calendar) {
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        int year = calendar.get(Calendar.YEAR) - 2000;
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
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
}

