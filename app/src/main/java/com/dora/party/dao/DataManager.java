package com.dora.party.dao;


import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.dora.party.domain.Cost;
import com.dora.party.domain.Donation;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;

public class DataManager {

    static final private String TAG = "DataManager";

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
    }

    public void saveCost(Cost cost) {

        cost.save();
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
}
