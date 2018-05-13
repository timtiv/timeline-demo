package com.tim.cubo;

import android.app.Application;
import android.content.Context;

public class Cubo extends Application {

    private static Cubo cubo;

    @Override
    public void onCreate() {
        super.onCreate();
        cubo = this;
    }

    public static Context getAppContext() {
        return cubo;
    }

}

