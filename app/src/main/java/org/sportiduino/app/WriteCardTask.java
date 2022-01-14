package org.sportiduino.app;

import android.os.AsyncTask;

import org.sportiduino.app.sportiduino.Card;
import org.sportiduino.app.sportiduino.Util;
import org.sportiduino.app.sportiduino.ReadWriteCardException;

public class WriteCardTask extends AsyncTask<Void, Void, Void> {
    final private Card card;
    Util.Callback showText;

    WriteCardTask(Card card, Util.Callback showText) {
        this.card = card;
        this.showText = showText;
    }

    @Override
    protected void onPreExecute() {
        showText.call("Writing card, don't remove it!");
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            card.write();
            showText.call("Data written to card successfully");
        } catch (ReadWriteCardException e) {
            showText.call("Writing card failed!");
        }

        return null;
    }
}
