package org.sportiduino.app.sportiduino;

import static org.sportiduino.app.sportiduino.Constants.*;

import android.util.Log;

import org.sportiduino.app.Password;

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
    public String parseData(byte[][] data) {
        switch (type) {
            case MASTER_SET_TIME:
                return "Time Master card";
            case MASTER_SET_NUMBER:
                return "Number Master card";
            case MASTER_GET_STATE:
                State state = new State(data);
                return "State Master card\n" + state.toString();
            case MASTER_SLEEP:
                return "Sleep Master card";
            case MASTER_READ_BACKUP:
                return parseBackupMaster(data);
            case MASTER_CONFIG:
                return "Config Master card";
            default:
                return "Unknown card type";
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
        int stationNumber = dataPage4[0] & 0xff;
        long timeHigh12bits = 0;
        long initTime = 0;
        StringBuilder ret = new StringBuilder("Backup Master card");
        if (dataPage4[3] > 0) { // have timestamp
            ret.append("\n").append("Station No ").append(stationNumber);
            for (byte[] datum : data) {
                if (timeHigh12bits == 0) {
                    initTime = Util.toUint32(datum);
                    timeHigh12bits = initTime & 0xfff00000;
                    continue;
                }

                int cardNum = (datum[0] & 0xff) << 8;
                cardNum |= datum[1] & 0xff;
                cardNum >>= 4;

                if (cardNum == 0) {
                    continue;
                }

                long punchTime = Util.toUint32(datum) & 0xfffff | timeHigh12bits;
                if (punchTime < initTime) {
                    punchTime += 0x100000;
                }
                ret.append(String.format("\n%1$4s", cardNum));
                ret.append(" - ").append(Util.dformat.format(new Date(punchTime * 1000)));
            }
        }
        return ret.toString();
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
}

