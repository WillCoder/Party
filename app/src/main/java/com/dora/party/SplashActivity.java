package com.dora.party;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.dora.party.util.SystemUiHider;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONObject;

import java.io.File;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class SplashActivity extends Activity {

    private static final String TAG = "SplashActivity";

    private static final int SPLASH_TIME = 1000;

    private Thread splashTread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(SPLASH_TIME);
                        requestData();
                    }

                } catch (InterruptedException e) {

                } finally {
                    finish();
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
            }
        };

        splashTread.start();
    }

    public void requestData() {


        File file = new File(Environment.getExternalStorageDirectory(), "1416824357183.test");
        Log.d(TAG, "File path --> " + file.getPath());
        String key = "123";
        String token = "VCgCBGYPx-5RrmkdC85i0NhNI44q8YorXovulTZ3";
        UploadManager uploadManager = new UploadManager();
        uploadManager.put(file, key, token,
                new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info, JSONObject response) {
                        Log.d(TAG, "complete ->>" + String.valueOf(info));
                    }
                }, null);
    }

}
