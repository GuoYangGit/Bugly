package com.gy.bugly

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.meituan.android.walle.WalleChannelReader

import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta

/**
 * Created by 韩莫熙 on 2017/5/26.
 */

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        /**
         * 已经接入Bugly用户改用上面的初始化方法,不影响原有的crash上报功能;
         * init方法会自动检测更新，不需要再手动调用Beta.checkUpdate(),如需增加自动检查时机可以使用Beta.checkUpdate(false,false);
         * 参数1： applicationContext
         * 参数2：appId
         * 参数3：是否开启debug
         */
        val channel = WalleChannelReader.getChannel(this,"default")
        Bugly.setAppChannel(this, channel)
        Bugly.init(applicationContext, "8d92e577fd", true)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(base)
        // 安装tinker
        Beta.installTinker()
    }
}
