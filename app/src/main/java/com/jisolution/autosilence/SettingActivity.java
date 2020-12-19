package com.jisolution.autosilence;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.jisolution.autosilence.DB.DBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class SettingActivity extends AppCompatActivity {
    SwitchMaterial notification,vibration, background;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean vibe,noti,backgr;
    AdView mAdView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        vibration=findViewById(R.id.vibration);
        notification=findViewById(R.id.notification);
        background = findViewById(R.id.background_switch);
        textView=(TextView)findViewById(R.id.txt1);
        ad();

        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        vibe=sharedPreferences.getBoolean("vibe", true);
        backgr = sharedPreferences.getBoolean("background", true);
        background.setChecked(backgr);
        noti=sharedPreferences.getBoolean("noti", true);
        editor =sharedPreferences.edit();
        vibration.setChecked(vibe);
        notification.setChecked(noti);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(SettingActivity.this);
                builder1.setMessage("Please Follow the instruction\n1-Go to settings->Battery->App Launch\n" +
                        "2-Find application Location Base Auto-Silent\n3-Uncheck the switch button\n" +
                        "4-Allow all settings and press ok");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });

        vibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    vibration.setChecked(true);
                    editor.putBoolean("vibe", true);
                }else{
                    vibration.setChecked(false);
                    editor.putBoolean("vibe", false);
                }
                editor.apply();
            }
        });

        notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    notification.setChecked(true);
                    editor.putBoolean("noti", true);
                }else{
                    notification.setChecked(false);
                    editor.putBoolean("noti", false);
                }
                editor.apply();
            }
        });

        background.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b){
                background.setChecked(true);
                editor.putBoolean("background", true);
                DBHelper dbHelper = new DBHelper(this);
                ArrayList<HashMap<String, String>> list = dbHelper.GetUsers();
                if (list.size() > 0) {
                    WorkRequest registerWorkRequest = new PeriodicWorkRequest.Builder(GeofenceRegisterWorker.class, 30, TimeUnit.MINUTES).addTag("worker").build();
                    WorkManager.getInstance(this).enqueue(registerWorkRequest);
                }
            }else{
                background.setChecked(false);
                editor.putBoolean("background", false);
                DBHelper dbHelper = new DBHelper(this);
                ArrayList<HashMap<String, String>> list = dbHelper.GetUsers();
                GeoFenceHelper geoFenceHelper = new GeoFenceHelper(this);
                geoFenceHelper.removeAllGeofence(list);
                WorkManager.getInstance(SettingActivity.this).cancelAllWork();
            }
            editor.apply();
        });

        Log.e("Vibe", "onCreate: " +vibration.isChecked());
        Log.e("Noti", "onCreate: "+notification.isChecked() );
    }
    private void ad() {
        mAdView = findViewById(R.id.adView);
        MobileAds.initialize(this,"ca-app-pub-4962376926519245/1463934372");
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }
}