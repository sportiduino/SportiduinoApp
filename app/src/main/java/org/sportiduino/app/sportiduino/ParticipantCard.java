package org.sportiduino.app.sportiduino;

import static org.sportiduino.app.sportiduino.Constants.CARD_PAGE_INFO1;
import static org.sportiduino.app.sportiduino.Constants.CARD_PAGE_INIT;
import static org.sportiduino.app.sportiduino.Constants.CARD_PAGE_INIT_TIME;
import static org.sportiduino.app.sportiduino.Constants.CARD_PAGE_START;
import static org.sportiduino.app.sportiduino.Constants.FW_PROTO_VERSION;

import org.sportiduino.app.App;
import org.sportiduino.app.R;

import java.util.Date;

public class ParticipantCard extends Card {
    int cardNumber;
    long cardInitTimestamp;

    public ParticipantCard(CardAdapter adapter, int cardNumber, long cardInitTimestamp) {
        this(adapter, cardNumber);
        this.cardInitTimestamp = cardInitTimestamp;
    }

    public ParticipantCard(CardAdapter adapter, int cardNumber) {
        super(adapter);

        this.type = CardType.ORDINARY;
        this.cardNumber = cardNumber;
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
    public String parseData(byte[][] data) {
        if (type == CardType.UNKNOWN) {
            return App.str(R.string.unknown_card_type);
        }

        String str = App.str(R.string.participant_card_no_) + cardNumber;
        str += App.str(R.string.clear_time_) + Util.dformat.format(new Date(cardInitTimestamp * 1000));
        long timeHighPart = cardInitTimestamp & 0xFF000000;
        for (byte[] datum : data) {
            int cp = datum[0] & 0xFF;
            if (cp == 0) {
                break;
            }
            long punchTimestamp = (Util.toUint32(datum) & 0xFFFFFF) + timeHighPart;
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
            str += " - " + Util.dformat.format(new Date(punchTimestamp * 1000));
        }
        return str;
    }

    @Override
    protected void writeImpl() throws ReadWriteCardException {
        adapter.clear(CARD_PAGE_INFO1, adapter.getMaxPage());
        long currentTimestamp = System.currentTimeMillis() / 1000;
        adapter.writePage(CARD_PAGE_INIT_TIME, Util.fromUint32(currentTimestamp));
        final byte[] cardNumberArray = Util.fromUint16(cardNumber);
        final byte[] dataPageInit = {cardNumberArray[0], cardNumberArray[1], 0, FW_PROTO_VERSION};
        adapter.writePage(CARD_PAGE_INIT, dataPageInit);
    }
}
