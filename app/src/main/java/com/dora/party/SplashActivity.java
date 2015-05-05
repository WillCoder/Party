package com.dora.party;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.avos.avoscloud.AVOSCloud;
import com.dora.party.dao.DataManager;


public class SplashActivity extends Activity {

    private static final String TAG = "SplashActivity";

    private static final int SPLASH_TIME = 3000;

    private Thread splashTread;

    private String AppId = "mas23lvpoxhe5ixk3ec1y74w1qhqtkfod8yja77kwefd3f5q";

    private String AppKey = "3zvdmol6o2ymhxi89l2s57yovo81lblb2so0eaopnuubfce9";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AVOSCloud.initialize(this, AppId, AppKey);

        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(SPLASH_TIME);
                    }

                } catch (InterruptedException e) {

                } finally {
                    startNext();
                }
            }
        };

        splashTread.start();

        requestData();
    }

    public void requestData() {

        DataManager.getInstance(this).syncData();
    }

    private void startNext() {
        finish();
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
    }
}
