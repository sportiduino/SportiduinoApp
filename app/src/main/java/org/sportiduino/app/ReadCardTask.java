package org.sportiduino.app;

import android.os.AsyncTask;

import org.sportiduino.app.sportiduino.Card;
import org.sportiduino.app.sportiduino.CardAdapter;
import org.sportiduino.app.sportiduino.ReadWriteCardException;
import org.sportiduino.app.sportiduino.Util;

class ReadCardTask extends AsyncTask<Void, Void, Void> {
    CardAdapter cardAdapter;
    Card card;
    Util.Callback showText;
    Util.Callback setTagType;
    byte[][] buffer;

    ReadCardTask(CardAdapter adapter, Util.Callback showText, Util.Callback setTagType) {
        this.cardAdapter = adapter;
        this.showText = showText;
        this.setTagType = setTagType;
    }

    @Override
    protected void onPreExecute() {
        showText.call("Reading card, don't remove it!");
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            cardAdapter.connect();
            card = Card.detectCard(cardAdapter);
            buffer = card.read();
        } catch (ReadWriteCardException e) {
            showText.call(Util.error("Reading card failed!"));
        } finally {
            cardAdapter.close();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        setTagType.call(card.adapter.tagType.name());
        showText.call(card.parseData(buffer));
        //switch (card.type) {
        //    case UNKNOWN:
        //        showText.call("Unknown card type");
        //        break;
        //    case ORDINARY:
        //        String str = "Participant card No: " + cardNumber;
        //        str += "\nClear time: " + Util.dformat.format(new Date(cardInitTimestamp * 1000));
        //        long timeHighPart = cardInitTimestamp & 0xFF000000;
        //        for (int i = 0; i < card.getMaxPage(); ++i) {
        //            int cp = buffer[i][0] & 0xFF;
        //            if (cp == 0) {
        //                break;
        //            }
        //            long punchTimestamp = (Util.toUint32(buffer[i]) & 0xFFFFFF) + timeHighPart;
        //            if (punchTimestamp < cardInitTimestamp) {
        //                punchTimestamp += 0x1000000;
        //            }
        //            str += "\n";
        //            String cpStr = String.valueOf(cp);
        //            switch (cp) {
        //                case Config.START_STATION:
        //                    cpStr = "Start";
        //                    break;
        //                case Config.FINISH_STATION:
        //                    cpStr = "Finish";
        //                    break;
        //            }
        //            str += String.format("%1$6s", cpStr);
        //            str += " - " + Util.dformat.format(new Date(punchTimestamp * 1000));
        //        }
        //        showText.call(str);
        //        break;
        //    case MASTER_SET_TIME:
        //        showText.call("Time Master card");
        //        break;
        //    case MASTER_SET_NUMBER:
        //        showText.call("Number Master card");
        //        break;
        //    case MASTER_GET_STATE:
        //        State state = new State(buffer);
        //        showText.call("State Master card\n" + state.toString());
        //        break;
        //    case MASTER_SLEEP:
        //        showText.call("Sleep Master card");
        //        break;
        //    case MASTER_READ_BACKUP:
        //        showText.call("Backup Master card");
        //        break;
        //    case MASTER_SET_PASS:
        //        showText.call("Password Master card");
        //        break;
        //    default:
        //        showText.call("Fail to read card!!!");
        //        break;
        //}
    }
}

