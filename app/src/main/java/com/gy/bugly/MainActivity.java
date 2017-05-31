package com.gy.bugly;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tencent.bugly.beta.Beta;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        findViewById( R.id.checkupdata ).setOnClickListener( v -> Beta.checkUpgrade() );
        findViewById( R.id.checkfix ).setOnClickListener( v -> Toast.makeText( MainActivity.this
                , "This is 1.0.2 fix apk", Toast.LENGTH_SHORT ).show() );
    }
}
