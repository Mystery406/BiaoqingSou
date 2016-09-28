package com.biaoqingsou.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Mystery406.
 */

public class ToastUtil {
    private static Context mContext;


    private ToastUtil() {
    }


    public static void init(Context context) {
        mContext = context;
    }

    public static void showShort(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }


}
