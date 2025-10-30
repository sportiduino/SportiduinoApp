package org.sportiduino.app;

import org.json.JSONObject;

interface Callback {
    void onOk(JSONObject json);
    void onError(Exception e);
}

public class CardWebServiceCallback implements Callback {

    @Override
    public void onOk(JSONObject json) {

    }

    @Override
    public void onError(Exception e) {

    }
}
