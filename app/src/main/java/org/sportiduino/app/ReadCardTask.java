package org.sportiduino.app;

import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;

import org.sportiduino.app.sportiduino.Card;
import org.sportiduino.app.sportiduino.CardType;
import org.sportiduino.app.sportiduino.Config;
import org.sportiduino.app.sportiduino.State;
import org.sportiduino.app.sportiduino.Util;

import java.util.Date;

class ReadCardTask extends AsyncTask<Void, Void, Void> {
    static final int CARD_PAGE_INIT = 4;
    static final int CARD_PAGE_INIT_TIME = 5;
    static final int CARD_PAGE_LAST_RECORD_INFO = 6;
    static final int CARD_PAGE_INFO1 = 6;
    static final int CARD_PAGE_INFO2 = 7;
    static final int CARD_PAGE_START = 8;
    static final int CARD_PAGE_PASS = 5;
    static final int CARD_PAGE_DATE = 6;
    static final int CARD_PAGE_TIME = 7;
    static final int CARD_PAGE_STATION_NUM = 6;
    static final int CARD_PAGE_BACKUP_START = 6;
    static final byte MASTER_CARD_SIGN = (byte) 0xFF;

    public interface Callback {
        void call(String str);
    }
    Card card;
    int cardNumber;
    long cardInitTimestamp;
    Callback callback;

    final int numOfSector = 16;
    final int numOfBlockInSector = 4;
    byte[][] buffer = new byte[numOfSector * numOfBlockInSector][MifareClassic.BLOCK_SIZE];

    ReadCardTask(Card card, Callback callback) {
        this.card = card;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        callback.call("Reading Tag, don't remove it!");
    }

    @Override
    protected Void doInBackground(Void... params) {

        card.connect();

        byte[] data = card.readPage(CARD_PAGE_INIT);

        if (data[2] == MASTER_CARD_SIGN) {
            card.type = CardType.byValue(Util.byteToUint(data[1]));
            switch (card.type) {
                case MASTER_GET_STATE:
                    buffer = card.readPages(8, 12);
                    break;
                default:
                    card.type = CardType.UNKNOWN;
            }
        } else {
            card.type = CardType.ORDINARY;
            cardNumber = data[0] & 0xFF;
            cardNumber <<= 8;
            cardNumber |= data[1] & 0xFF;
            data = card.readPage(CARD_PAGE_INIT_TIME);
            cardInitTimestamp = Util.toUint32(data);
            buffer = card.readPages(CARD_PAGE_START, card.getMaxPage(), true);
        }

        card.close();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        //setText(textViewInfo.getText().toString() + "\n" + card.tagType.name());
        switch (card.type) {
            case UNKNOWN:
                callback.call("Unknown card type");
                break;
            case ORDINARY:
                String str = "Participant card No: " + cardNumber;
                str += "\nClear time: " + Util.dformat.format(new Date(cardInitTimestamp * 1000));
                long timeHighPart = cardInitTimestamp & 0xFF000000;
                //Log.i("timeHighPart", Long.toHexString(timeHighPart));
                for (int i = 0; i < card.getMaxPage(); ++i) {
                    int cp = buffer[i][0] & 0xFF;
                    if (cp == 0) {
                        break;
                    }
                    long punchTimestamp = (Util.toUint32(buffer[i]) & 0xFFFFFF) + timeHighPart;
                    if (punchTimestamp < cardInitTimestamp) {
                        punchTimestamp += 0x1000000;
                    }
                    str += "\n";
                    String cpStr = String.valueOf(cp);
                    switch (cp) {
                        case Config.START_STATION:
                            cpStr = "Start";
                            break;
                        case Config.FINISH_STATION:
                            cpStr = "Finish";
                            break;
                    }
                    str += String.format("%1$6s", cpStr);
                    str += " - " + Util.dformat.format(new Date(punchTimestamp * 1000));
                }
                callback.call(str);
                break;
            case MASTER_SET_TIME:
                callback.call("Time Master card");
                break;
            case MASTER_SET_NUMBER:
                callback.call("Number Master card");
                break;
            case MASTER_GET_STATE:
                State state = new State(buffer);
                callback.call(state.toString());
                break;
            case MASTER_SLEEP:
                callback.call("Sleep Master card");
                break;
            case MASTER_READ_BACKUP:
                callback.call("Backup Master card");
                break;
            case MASTER_SET_PASS:
                callback.call("Password Master card");
                break;
            default:
                callback.call("Fail to read card!!!");
                break;
        }
    }
}
