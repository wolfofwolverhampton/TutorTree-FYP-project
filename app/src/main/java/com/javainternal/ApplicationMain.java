package com.javainternal;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class ApplicationMain extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
