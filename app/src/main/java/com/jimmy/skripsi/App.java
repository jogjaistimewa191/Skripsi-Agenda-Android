package com.jimmy.skripsi;

import androidx.multidex.MultiDexApplication;

public class App extends MultiDexApplication {
    static App mInstance;

    public static synchronized App getInstance(){
        return mInstance;
    }

    public void setmInstance(App instance){
        App.mInstance = instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setmInstance(this);
    }


}
