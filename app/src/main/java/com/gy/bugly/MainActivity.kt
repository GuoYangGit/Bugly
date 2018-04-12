package com.gy.bugly

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.meituan.android.walle.WalleChannelReader

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val channel = WalleChannelReader.getChannel(this,"default")
        channel_tv.text = channel
        test_btn.setOnClickListener {
            Toast.makeText(this,"this is bugly",Toast.LENGTH_SHORT).show()
        }
    }
}
