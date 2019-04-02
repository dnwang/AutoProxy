package org.pinwheel.autoproxy.activity

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class KotlinActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        setContentView(textView)
        // show content
        textView.text = test(arrayOf("de", "nan", "wang"))
    }

    private fun test(args: Array<String>): String = args.let {
        var result = ""
        for (tmp: String in it) {
            result += tmp
        }
        return result
    }

}