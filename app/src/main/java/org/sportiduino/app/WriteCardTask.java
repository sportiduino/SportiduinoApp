package org.sportiduino.app;

import static org.sportiduino.app.sportiduino.Constants.CARD_PAGE_INIT;
import static org.sportiduino.app.sportiduino.Constants.FW_PROTO_VERSION;
import static org.sportiduino.app.sportiduino.Constants.MASTER_CARD_SIGN;

import android.os.AsyncTask;

import org.sportiduino.app.sportiduino.Card;
import org.sportiduino.app.sportiduino.CardType;
import org.sportiduino.app.sportiduino.Util;
import org.sportiduino.app.sportiduino.WriteCardException;

public class WriteCardTask extends AsyncTask<Void, Void, Void> {
    Card card;
    Util.Callback showText;
    byte[] password;

    //final int numOfBlockInSector = 4;
    //byte[][] buffer = new byte[numOfSector * numOfBlockInSector][MifareClassic.BLOCK_SIZE];

    WriteCardTask(Card card, Util.Callback showText, byte[] password) {
        this.card = card;
        this.showText = showText;
        this.password = password;
    }

    @Override
    protected void onPreExecute() {
        showText.call("Writing card, don't remove it!");
    }

    @Override
    protected Void doInBackground(Void... params) {

        card.connect();

        try {
            switch (card.type) {
                case ORDINARY:
                    break;
                case MASTER_GET_STATE:
                    byte[][] data = new byte[][]{
                            {0, (byte) CardType.MASTER_GET_STATE.value, MASTER_CARD_SIGN, FW_PROTO_VERSION},
                            {password[0], password[1], password[3], 0}
                    };
                    card.writePages(CARD_PAGE_INIT, data, 2);
                    break;
            }
        } catch (WriteCardException e) {
            showText.call("Writing card failed!");
        }

        card.close();

        return null;
    }
}
