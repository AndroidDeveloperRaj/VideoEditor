package com.bs.videoeditor.application;

import android.app.Application;
import android.content.Context;

import com.bsoft.core.GAApplication;


public class MyApplication extends GAApplication {
    private static MyApplication mySelf;

    public static MyApplication self() {
        return mySelf;
    }


    public void onCreate() {
        super.onCreate();
        mySelf = this;

    }

    public static Context getAppContext() {
        return mySelf;
    }
}