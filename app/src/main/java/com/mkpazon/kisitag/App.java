package com.mkpazon.kisitag;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by mkpazon on 05/08/2017.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree() {
            @Override
            protected String createStackElementTag(StackTraceElement element) {
                return super.createStackElementTag(element) + ":" + element.getLineNumber();
            }
        });
    }
}
