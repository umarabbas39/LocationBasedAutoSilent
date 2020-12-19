package com.jisolution.autosilence;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.jisolution.autosilence.DB.DBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

//This receiver will receive call everytime device is restarted so in onReceive method of it, I am getting entries from database and setting geofence on them.
public class BootReceiver extends BroadcastReceiver {

    DBHelper dbHelper;
    ArrayList<HashMap<String,String >> userDetail;
    private GeofencingClient geofencingClient;
    private GeoFenceHelper geoFenceHelper;


    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        boolean background = sharedPreferences.getBoolean("background", true);
        if(background){
            dbHelper=new DBHelper(context);
            userDetail=dbHelper.GetUsers();
            geoFenceHelper = new GeoFenceHelper(context);
            geofencingClient = LocationServices.getGeofencingClient(context);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(new Intent(context, ForegroundService.class));
//            }else{
//                context.startService(new Intent(context, ForegroundService.class));
//            }
            String id = "";
            for (int i = 0 ; i<userDetail.size(); i++){
                List<Geofence> geofenceList = new ArrayList<>();
                id = userDetail.get(i).get("id");
                double lat = Double.parseDouble(userDetail.get(i).get("lat"));
                double lng = Double.parseDouble(userDetail.get(i).get("lng"));
                LatLng latlng = new LatLng(lat, lng);
                float radius = Float.parseFloat(userDetail.get(i).get("radius"));
                Geofence geofence = geoFenceHelper.getGeofence(id, latlng, radius, Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
                geofenceList.add(geofence);
                GeofencingRequest geofencingRequest = geoFenceHelper.getGeofenceRequest(geofenceList);
                PendingIntent pendingIntent = geoFenceHelper.getPendingIntent(Integer.parseInt(id));
                geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Log.d("BootReceiver", "Success:Geofenceadded");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                String errorMessage = geoFenceHelper.getErrorString(e);
                                Log.d("BootReceiver", "onFaliure" + errorMessage);
                            }
                        });
            }
            WorkRequest registerWorkRequest = new PeriodicWorkRequest.Builder(GeofenceRegisterWorker.class, 30, TimeUnit.MINUTES).build();
            WorkManager.getInstance(context).enqueue(registerWorkRequest);
        }

    }
}