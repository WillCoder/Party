package com.dora.party.dao;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.dora.party.domain.Cost;
import com.dora.party.domain.Donation;
import com.orm.SugarRecord;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

    private final static String TAG = "DataManager";

    private final static String TOKEN_ID = "55488d4de4b03fd834536ecf";

    private final static String TOKEN_NAME = "PartyToken";

    private final static String DATA_KEY = "database";

    private static DataManager dataManager;

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
    }

    public void saveCost(Cost cost) {

        cost.save();
        syncDataToServer();
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
    }

    public void syncDataToServer() {

        AVQuery<AVObject> query = new AVQuery<>(TOKEN_NAME);
        try {

            final String databaseName = readMetaDataFromApplication("DATABASE");
            final File database = context.getDatabasePath(databaseName);
            final AVFile file = AVFile.withAbsoluteLocalPath(databaseName, database.getPath());

            query.getInBackground(TOKEN_ID, new GetCallback<AVObject>() {

                @Override
                public void done(AVObject avObject, AVException e) {

                    if (e == null) {

                        avObject.put(DATA_KEY, file);
                        avObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    Log.i("LeanCloud", "Save successfully.");
                                } else {
                                    Log.e("LeanCloud", "Save failed.");
                                }
                            }
                        });
                    }
                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initServer() {

        AVObject tokenObj = new AVObject(TOKEN_NAME);
        tokenObj.saveInBackground();
    }

    public void syncDataFromServer() {

        final String databaseName = readMetaDataFromApplication("DATABASE");
        final File database = context.getDatabasePath(databaseName);

        AVQuery<AVObject> query = new AVQuery<>(TOKEN_NAME);
        try {
            AVFile avFile = (AVFile) query.get(TOKEN_ID).get(DATA_KEY);

            if (avFile != null) {
                try {
                    if (database.exists()) {
                        database.delete();
                    }
                    saveBytesToFile(avFile.getData(), database);
                } catch (AVException e1) {
                    e1.printStackTrace();
                }
            }

        } catch (AVException e) {
            e.printStackTrace();
        }
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

    static private void saveBytesToFile(byte[] fileBytes, File yourFile) {

        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(yourFile));
            bos.write(fileBytes);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
