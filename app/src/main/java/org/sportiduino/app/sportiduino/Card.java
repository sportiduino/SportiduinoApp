package org.sportiduino.app.sportiduino;

import static org.sportiduino.app.sportiduino.Constants.*;

import org.sportiduino.app.Password;


public abstract class Card {
    public CardType type = CardType.UNKNOWN;
    public CardAdapter adapter;
    protected byte[] dataPage4;

    public Card(CardAdapter adapter) {
        this.adapter = adapter;
    }

    public abstract byte[][] read() throws ReadWriteCardException;
    public abstract String parseData(byte[][] data);

    protected abstract void writeImpl() throws ReadWriteCardException;

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

        if (data[2] == MASTER_CARD_SIGN) {
            CardType type = CardType.byValue(Util.byteToUint(data[1]));
            MasterCard masterCard = new MasterCard(adapter, type, Password.defaultPassword());
            masterCard.dataPage4 = data;
            return masterCard;
        } else {
            int cardNumber = data[0] & 0xFF;
            cardNumber <<= 8;
            cardNumber |= data[1] & 0xFF;
            data = adapter.readPage(CARD_PAGE_INIT_TIME);
            long cardInitTimestamp = Util.toUint32(data);
            return new ParticipantCard(adapter, cardNumber, cardInitTimestamp);
        }
         
    }
}

