package com.gy.bugly

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.meituan.android.walle.WalleChannelReader

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //获取我们写入的渠道信息
        val channel = WalleChannelReader.getChannel(this,"default")
        //赋值给textview显示
        channel_tv.text = channel
        //这里我们写一个点击事件，点击tosat
        test_btn.setOnClickListener {
            toast("${2/1}")
        }
    }
}
