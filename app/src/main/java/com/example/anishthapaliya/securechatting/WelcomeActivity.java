package com.example.anishthapaliya.securechatting;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Thread thread = new Thread() {
            public void run() {
                try {
                    sleep(1000);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    Intent startIntent=new Intent(WelcomeActivity.this,StartActivity.class);
                    startActivity(startIntent);
                    finish();
                }
            }
        };

        thread.start();
    }
    public void onPause() {

        super.onPause();
    }
}