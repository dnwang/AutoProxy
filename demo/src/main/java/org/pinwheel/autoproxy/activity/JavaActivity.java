package org.pinwheel.autoproxy.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Copyright (C), 2019 <br>
 * <br>
 * All rights reserved <br>
 * <br>
 *
 * @author dnwang
 * @version 2019/3/21,13:58
 */
public class JavaActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        setContentView(textView);
        // show content
        textView.setText(test("input", 1000));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public String test(String arg0, int arg1) {
        return "test: " + arg0 + ", " + arg1 + "\n";
    }

}