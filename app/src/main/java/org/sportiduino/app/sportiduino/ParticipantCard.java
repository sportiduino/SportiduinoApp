package org.sportiduino.app.sportiduino;

import static org.sportiduino.app.sportiduino.Constants.*;

import org.sportiduino.app.App;
import org.sportiduino.app.R;

import java.util.Date;

public class ParticipantCard extends Card {
    int cardNumber;
    long cardInitTimestamp;
    boolean fastPunch;

    public ParticipantCard(CardAdapter adapter, int cardNumber, boolean fastPunch, long cardInitTimestamp) {
        this(adapter, cardNumber, fastPunch);
        this.cardInitTimestamp = cardInitTimestamp;
    }

    public ParticipantCard(CardAdapter adapter, int cardNumber, boolean fastPunch) {
        super(adapter);

        this.type = CardType.ORDINARY;
        this.cardNumber = cardNumber;
        this.fastPunch = fastPunch;
    }

    @Override
    public byte[][] read() throws ReadWriteCardException {
        if (cardNumber == 0) {
            type = CardType.UNKNOWN;
            return new byte[0][0];
        }
        return adapter.readPages(CARD_PAGE_START, adapter.getMaxPage(), true);
    }

    @Override
    public CharSequence parseData(byte[][] data) {
        if (type == CardType.UNKNOWN) {
            return App.str(R.string.unknown_card_type);
        }

        String str = App.str(R.string.participant_card_no_) + cardNumber;
        if (fastPunch) {
            str += String.format(" (%s)", App.str(R.string.fast_punch));
        }
        str += "\n" + App.str(R.string.clear_time_) + Util.dformat.format(new Date(cardInitTimestamp*1000));
        str += "\n" + App.str(R.string.record_count) + " %d";
        int recordCount = 0;
        long timeHighPart = cardInitTimestamp & 0xff000000;
        for (byte[] datum : data) {
            int cp = datum[0] & 0xff;
            if (cp == 0) {
                break;
            }
            long punchTimestamp = (Util.toUint32(datum) & 0xffffff) + timeHighPart;
            if (punchTimestamp < cardInitTimestamp) {
                punchTimestamp += 0x1000000;
            }
            str += "\n";
            String cpStr = String.valueOf(cp);
            switch (cp) {
                case Config.START_STATION:
                    cpStr = App.str(R.string.start);
                    break;
                case Config.FINISH_STATION:
                    cpStr = App.str(R.string.finish);
                    break;
            }
            str += String.format("%1$6s", cpStr);
            str += " - " + Util.dformat.format(new Date(punchTimestamp*1000));
            ++recordCount;
        }
        return String.format(str, recordCount);
    }

    @Override
    protected void writeImpl() throws ReadWriteCardException {
        adapter.clear(CARD_PAGE_INFO1, adapter.getMaxPage());
        if (fastPunch) {
            adapter.writePage(CARD_PAGE_INFO1, new byte[]{0, 0, 0, FAST_PUNCH_SIGN});
        }
        long currentTimestamp = System.currentTimeMillis() / 1000;
        adapter.writePage(CARD_PAGE_INIT_TIME, Util.fromUint32(currentTimestamp));
        if (cardNumber > 0) {  // else cleaning only
            final byte[] cardNumberArray = Util.fromUint16(cardNumber);
            final byte[] dataPageInit = {cardNumberArray[0], cardNumberArray[1], 0, FW_PROTO_VERSION};
            adapter.writePage(CARD_PAGE_INIT, dataPageInit);
        }
    }
}
