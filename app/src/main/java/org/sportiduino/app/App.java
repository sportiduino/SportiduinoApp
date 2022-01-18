package org.sportiduino.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.core.content.ContextCompat;

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

    public static int color(int id) {
        return ContextCompat.getColor(context, id);
    }
}
