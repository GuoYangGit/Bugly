package com.gy.bugly

import android.content.Context
import android.widget.Toast

/**
 * Created by guoyang on 2018/4/15.
 * github https://github.com/GuoYangGit
 * QQ:352391291
 */
fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}