package org.sportiduino.app;

import android.content.Context;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CardWebService {
    private Context context;
    private String url;

    private CardWebServiceCallback callback;

    public CardWebService(Context context, String url, CardWebServiceCallback callback) {
        this.setContext(context);
        this.setUrl(url);

        this.registerCallback(callback);
    }

    public void send(CharSequence data) {
        Context context = this.context;
        String url = this.url;

        Callback callback = this.callback;

        if (!isValidUrl(url)) {
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(context));

        JSONObject json = new JSONObject();
        try {
            json.put("data", data);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, getUrlWithoutUserInfo(url), json, callback::onOk, callback::onError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");

                String credentials = getUserInfoFromUrl(url);
                if (credentials != null) {
                    String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                    headers.put("Authorization", auth);
                }

                return headers;
            }
        };

        queue.add(request);
    }

    private void setContext(Context context) {
        this.context = context;
    }

    private void setUrl(String url) {
        this.url = url;
    }

    private void registerCallback(CardWebServiceCallback callback) {
        this.callback = callback;
    }

    private static Boolean isValidUrl(String spec) {
        try {
            new URL(spec);

            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private static String getUserInfoFromUrl(String location) {
        try {
            URL url = new URL(location);

            String userInfo = url.getUserInfo();

            if (userInfo != null) {
                String[] userInfoArray = userInfo.split(":");

                if (userInfoArray.length == 2) {
                    return userInfo;
                }
            }

        } catch (MalformedURLException ignored) {}

        return null;
    }

    private static String getUrlWithoutUserInfo(String location) {
        String userInfo = getUserInfoFromUrl(location);

        if (userInfo == null) {
            return location;
        } else {
            return location.replace(userInfo + "@", "");
        }
    }
}
