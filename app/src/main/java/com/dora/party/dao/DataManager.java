package com.dora.party.dao;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.SaveCallback;
import com.dora.party.domain.Cost;
import com.dora.party.domain.Donation;
import com.orm.SugarRecord;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

    private final static String TAG = "DataManager";

    private final static String TOKEN_NAME = "PartyToken";

    private final static String DATA_KEY = "database";

    private final static String TIME_STAMP = "timestamp";

    private static DataManager dataManager;

    private String token_id = null;

    private Context context;

    public DataManager(Context context) {
        this.context = context;
    }

    public static DataManager getInstance(Context context) {

        if (dataManager == null) {
            dataManager = new DataManager(context);
        }
        return dataManager;
    }

    public <T extends SugarRecord<?>> List<T> getDomains(Class<T> type) {

        try {
            return T.listAll(type);
        } catch (SQLiteException e) {

            Log.d(TAG, "Database access error! " + e);
            return new ArrayList<>();
        }
    }

    public void saveDonation(Donation donation) {

        donation.save();
        syncDataToServer();
        updateModifyTimeStamp();
    }

    public void saveCost(Cost cost) {

        cost.save();
        syncDataToServer();
        updateModifyTimeStamp();
    }

    public double getAllDonationValue() {

        double sum = 0;
        List<Donation> donations = getDomains(Donation.class);
        for (Donation donation : donations) {
            sum += donation.getValue();
        }
        return sum;
    }

    public double getAllCostValue() {
        double sum = 0;
        List<Cost> costs = getDomains(Cost.class);
        for (Cost cost : costs) {
            sum += cost.getValue();
        }
        return sum;
    }

    public void clearAllData() {

        SugarRecord.deleteAll(Donation.class);
        SugarRecord.deleteAll(Cost.class);
        syncDataToServer();
        updateModifyTimeStamp();
    }

    private void updateModifyTimeStamp() {

        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putLong(TIME_STAMP, System.currentTimeMillis());
        editor.apply();
    }

    private long getModifyTimeStamp() {

        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);

        return sp.getLong(TIME_STAMP, 0);
    }

    public void syncDataToServer() {

        AVQuery<AVObject> query = new AVQuery<>(TOKEN_NAME);
        try {

            final String databaseName = readMetaDataFromApplication("DATABASE");
            final File database = context.getDatabasePath(databaseName);
            final AVFile file = AVFile.withAbsoluteLocalPath(databaseName, database.getPath());

            query.getInBackground(token_id, new GetCallback<AVObject>() {

                @Override
                public void done(AVObject avObject, AVException e) {

                    if (e == null) {

                        avObject.put(DATA_KEY, file);
                        avObject.put(TIME_STAMP, getModifyTimeStamp());
                        avObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    Log.i(TAG, "Save successfully.");
                                } else {
                                    Log.e(TAG, "Save failed.");
                                }
                            }
                        });
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void syncDataFromServer() {

        AVQuery<AVObject> query = new AVQuery<>(TOKEN_NAME);
        query.findInBackground(
                new FindCallback<AVObject>() {
                    public void done(List<AVObject> avObjects, AVException e) {
                        if (e == null) {
                            token_id = avObjects.get(0).getObjectId();
                            overrideFromServer();
                        } else {
                            Log.d(TAG, "Error ->> " + e.getMessage());
                        }
                    }
                }

        );
    }

    private void overrideFromServer() {

        final String databaseName = readMetaDataFromApplication("DATABASE");
        final File databaseFile = context.getDatabasePath(databaseName);

        AVQuery<AVObject> query = new AVQuery<>(TOKEN_NAME);
        query.getInBackground(token_id, new GetCallback<AVObject>() {

            @Override
            public void done(AVObject avObject, AVException e) {

                long remoteTimestamp = avObject.getLong(TIME_STAMP);
                long localTimestamp = getModifyTimeStamp();

                if (remoteTimestamp > localTimestamp) {

                    AVFile avFile = avObject.getAVFile(DATA_KEY);
                    avFile.getDataInBackground(new GetDataCallback() {
                        public void done(byte[] data, AVException e) {

                            if (!databaseFile.getParentFile().exists()) {
                                databaseFile.getParentFile().mkdir();
                            }
                            saveBytesToFile(data, databaseFile);
                        }
                    });
                }
            }
        });
    }

    private String readMetaDataFromApplication(String key) {
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            return appInfo.metaData.getString(key);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    static private void saveBytesToFile(byte[] fileBytes, File file) {

        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(fileBytes);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
