package com.biaoqingsou;

import android.app.Application;

import com.biaoqingsou.util.ToastUtil;

/**
 * Created by Mystery406.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtil.init(getApplicationContext());
    }
}
