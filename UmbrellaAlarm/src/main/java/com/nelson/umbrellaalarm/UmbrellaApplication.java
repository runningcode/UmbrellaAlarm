package com.nelson.umbrellaalarm;

import android.app.Application;

public class UmbrellaApplication extends Application {

    @Override
    public void onCreate() {
        // workaround for http://code.google.com/p/android/issues/detail?id=20915
        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException ignored) {
        }
        super.onCreate();
    }
}
