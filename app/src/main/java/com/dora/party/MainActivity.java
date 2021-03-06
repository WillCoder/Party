package com.dora.party;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.dora.party.dao.DataManager;
import com.dora.party.domain.Cost;
import com.dora.party.domain.Donation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity {

    static final private String TAG = "MainActivity";

    DataManager dataManager = null;

    @InjectView(R.id.donation_list)
    ListView donationList;

    @InjectView(R.id.cost_list)
    ListView costList;

    @InjectView(R.id.total_value)
    TextView totalValueView;

    @InjectView(R.id.balance_value)
    TextView balanceValueView;

    private ArrayList<Map<String, String>> donationData;

    private ArrayList<Map<String, String>> costData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setTitle(R.string.party_name);

        dataManager = DataManager.getInstance(this);

        ButterKnife.inject(this);

        initDonationList();
        initCostList();

        refreshTotalValues();
    }

    private void refreshTotalValues() {

        BigDecimal allDonationValue = new BigDecimal(dataManager.getAllDonationValue());
        BigDecimal allCostValue = new BigDecimal(dataManager.getAllCostValue());

        totalValueView.setText(allDonationValue.toPlainString());

        BigDecimal balanceValue = allDonationValue.subtract(allCostValue);

        balanceValueView.setText(balanceValue.toPlainString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear) {

            dataManager.clearAllData();
            refreshListViews();
            refreshTotalValues();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.add_donation_button)
    public void onAddDonationButtonClick(ImageView addDonationButton) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.add_donation_dialog_title);

        LayoutInflater inflater = getLayoutInflater();
        final View viewContent = inflater.inflate(R.layout.donation_dialog_view, null);

        final EditText nameEditText = ButterKnife.findById(viewContent, R.id.name);
        final EditText valueEditText = ButterKnife.findById(viewContent, R.id.value);

        alert.setView(viewContent);
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Donation donation = new Donation(nameEditText.getText().toString(), Double.valueOf(valueEditText.getText().toString()));
                addDonation(donation);
                dataManager.saveDonation(donation);
                refreshListViews();
                refreshTotalValues();
            }
        });
        alert.setNegativeButton(R.string.cancel, null);
        AlertDialog donationDialog = alert.create();
        donationDialog.show();
    }

    private void refreshListViews() {

        donationList.setAdapter(new SimpleAdapter(this, donationData, R.layout.donation_item, new String[]{"name", "value"}, new int[]{R.id.name, R.id.value}));
        donationList.deferNotifyDataSetChanged();

        costList.setAdapter(new SimpleAdapter(this, costData, R.layout.cost_item, new String[]{"date", "name", "value"}, new int[]{R.id.date, R.id.name, R.id.value}));
        costList.deferNotifyDataSetChanged();
    }

    @OnClick(R.id.add_cost_button)
    public void onAddCostButtonClick(ImageView addCostButton) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.add_cost_dialog_title);

        LayoutInflater inflater = getLayoutInflater();
        final View viewContent = inflater.inflate(R.layout.cost_dialog_view, null);

        final CalendarView dateCalendarView = ButterKnife.findById(viewContent, R.id.date);
        final EditText nameEditText = ButterKnife.findById(viewContent, R.id.name);
        final EditText valueEditText = ButterKnife.findById(viewContent, R.id.value);

        alert.setView(viewContent);
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Cost cost = new Cost(new Date(dateCalendarView.getDate()), nameEditText.getText().toString(), Double.valueOf(valueEditText.getText().toString()));
                addCost(cost);
                dataManager.saveCost(cost);
                refreshListViews();
                refreshTotalValues();
            }
        });
        alert.setNegativeButton(R.string.cancel, null);
        AlertDialog costDialog = alert.create();
        costDialog.show();
    }

    private void addDonation(Donation donation) {

        HashMap<String, String> map = new HashMap<>();
        map.put("name", donation.getName());
        map.put("value", String.valueOf(donation.getValue()));
        donationData.add(map);
    }

    private void addCost(Cost cost) {

        HashMap<String, String> map = new HashMap<>();
        map.put("date", cost.getFormatedDate());
        map.put("name", cost.getName());
        map.put("value", String.valueOf(cost.getValue()));
        costData.add(map);
    }

    private void initDonationList() {

        List<Donation> donationDataList = dataManager.getDomains(Donation.class);
        donationData = new ArrayList<>();

        for (Donation donation : donationDataList) {
            addDonation(donation);
        }
        donationList.setAdapter(new SimpleAdapter(this, donationData, R.layout.donation_item, new String[]{"name", "value"}, new int[]{R.id.name, R.id.value}));
    }

    private void initCostList() {


        List<Cost> costDataList = dataManager.getDomains(Cost.class);
        costData = new ArrayList<>();

        for (Cost cost : costDataList) {
            addCost(cost);
        }
        costList.setAdapter(new SimpleAdapter(this, costData, R.layout.cost_item, new String[]{"date", "name", "value"}, new int[]{R.id.date, R.id.name, R.id.value}));
    }
}
