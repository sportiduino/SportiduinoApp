package org.sportiduino.app.sportiduino;

import static org.sportiduino.app.sportiduino.Constants.*;

import org.sportiduino.app.App;
import org.sportiduino.app.Password;
import org.sportiduino.app.R;

import java.util.Arrays;


public class Card {
    public CardType type = CardType.UNKNOWN;
    public CardAdapter adapter;
    protected byte[] dataPage4;

    private static final byte[] NTAG213_PAGE4_FACTORY_DATA = new byte[] {0x01, 0x03, (byte) 0xa0, 0x0c};
    private static final byte[] NTAG215_216_PAGE4_FACTORY_DATA = new byte[] {0x03, 0x00, (byte) 0xfe, 0x00};

    public Card(CardAdapter adapter) {
        this.adapter = adapter;
    }

    public byte[][] read() throws ReadWriteCardException {
        return new byte[0][0];
    }

    public CharSequence parseData(byte[][] data) {
        return App.str(R.string.unknown_card_type);
    }

    protected void writeImpl() throws ReadWriteCardException {}

    public void write() throws ReadWriteCardException {
        adapter.connect();
        try {
            writeImpl();
        } finally {
            adapter.close();
        }
    }

    public static Card detectCard(CardAdapter adapter) throws ReadWriteCardException {
        byte[] data = adapter.readPage(CARD_PAGE_INIT);

        if (Arrays.equals(Arrays.copyOf(data, 4), NTAG213_PAGE4_FACTORY_DATA) ||
                Arrays.equals(Arrays.copyOf(data, 4), NTAG215_216_PAGE4_FACTORY_DATA)) {
            // Card is not initialized
            return new Card(adapter);
        } else if (data[2] == MASTER_CARD_SIGN) {
            CardType type = CardType.byValue(Util.byteToUint(data[1]));
            MasterCard masterCard = new MasterCard(adapter, type, Password.defaultPassword());
            masterCard.dataPage4 = data;
            return masterCard;
        } else {
            int cardNumber = data[0] & 0xFF;
            cardNumber <<= 8;
            cardNumber |= data[1] & 0xFF;
            if (cardNumber == 0) {
                return new Card(adapter);
            }
            data = adapter.readPage(CARD_PAGE_INIT_TIME);
            long cardInitTimestamp = Util.toUint32(data);
            data = adapter.readPage(CARD_PAGE_INFO1);
            boolean fastPunch = (data[3] == FAST_PUNCH_SIGN);
            return new ParticipantCard(adapter, cardNumber, fastPunch, cardInitTimestamp);
        }
         
    }
}

