package com.jisolution.autosilence;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

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

public class GeofenceRegisterWorker extends Worker {
    DBHelper dbHelper;
    ArrayList<HashMap<String,String >> userDetail;
    GeoFenceHelper geoFenceHelper;
    Context context;
    public GeofenceRegisterWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @SuppressLint("MissingPermission")
    @Override
    public Result doWork() {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        boolean background = sharedPreferences.getBoolean("background", true);
        if(background){
            //This will register your geofences after evary 30 minutes
            dbHelper=new DBHelper(context);
            userDetail = dbHelper.GetUsers();
            geoFenceHelper = new GeoFenceHelper(context);
            geoFenceHelper.removeAllGeofence(userDetail);
            geoFenceHelper.addAllGeofence(userDetail);
            Log.d("GeofenceRegisterWorker", "Geofences added");
            // Indicate whether the work finished successfully with the Result
        }


        return Result.success();
    }
}
