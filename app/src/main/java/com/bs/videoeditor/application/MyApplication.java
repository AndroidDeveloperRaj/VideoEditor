package com.bs.videoeditor.application;

import android.app.Application;
import android.content.Context;



public class MyApplication extends Application {
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