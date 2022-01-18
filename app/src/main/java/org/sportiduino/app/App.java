package org.sportiduino.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

public class App extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static String str(int id) {
        return context.getString(id);
    }
}
