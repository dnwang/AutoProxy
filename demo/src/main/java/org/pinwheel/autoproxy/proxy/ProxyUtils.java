package org.pinwheel.autoproxy.proxy;

import android.util.Log;

import org.jetbrains.annotations.Nullable;
import org.pinwheel.autoProxy.Proxy;

/**
 * Copyright (C), 2019 <br>
 * <br>
 * All rights reserved <br>
 * <br>
 *
 * @author dnwang
 * @version 2019/3/19,00:09
 */
public final class ProxyUtils {

    private static final String TAG = "ProxyUtils";

    @Proxy(include = "org.pinwheel.autoproxy.activity.JavaActivity", exclude = "kotlin.*", method = "test")
    public static Proxy.R trace(Object target, String method, @Nullable Object... args) {
        Log.e(TAG, "[trace]: " + target.getClass() + "#" + method);
        return Proxy.R.SKIP("proxy result !!!!");
    }

//    @Proxy(include = "org.pinwheel.autoproxy.activity.KotlinActivity", method = "test")
//    public static Proxy.R traceKotlin(Object target, String method, @Nullable Object... args) {
//        Log.e(TAG, "[traceKotlin]: " + target.getClass() + "#" + method);
//        return Proxy.R.NOT_SKIP;
//    }

//    @Proxy(include = "android.*", exclude = "kotlin.*")
//    public static Proxy.R traceAndroid(Object target, String method, @Nullable Object... args) {
//        Log.e(TAG, "[traceAndroid]: " + target.getClass() + "#" + method);
//        return Proxy.R.NOT_SKIP;
//    }

}