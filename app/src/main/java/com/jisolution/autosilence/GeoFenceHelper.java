package com.jisolution.autosilence;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
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

public class GeoFenceHelper extends ContextWrapper {

    PendingIntent pendingIntent;
    private GeofencingClient geofencingClient;
    private GeoFenceHelper geoFenceHelper;
    private DBHelper dbHelper;

    public GeoFenceHelper(Context base) {
        super(base);
    }
    public GeofencingRequest getGeofenceRequest(List<Geofence> geofence){
        return new GeofencingRequest.Builder()
                //.addGeofence(geofence)//single or list of geofence
                .addGeofences(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    public Geofence getGeofence(String ID, LatLng latLng, float radius, int transitiontype){
        return new Geofence.Builder()
                .setCircularRegion(latLng.latitude,latLng.longitude,radius)
                .setRequestId(ID)//id should be unique for each geofence
                .setTransitionTypes(transitiontype)
                .setLoiteringDelay(3000)//3 sec delay between when we entert transtion type, dwell tt and leave tt geofence
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    public PendingIntent getPendingIntent(int id){
        //It will create a unique pending intent on unique id every time
        Intent intent=new Intent(this, GeofenceBroadcastReciver.class);
//pendingIntent=pendingIntent.getService(this,2607,intent,
//        pendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntent=PendingIntent.getBroadcast(this,id,intent, pendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public void removeAllGeofence(ArrayList<HashMap<String,String >> userDetail){
//        GoogleApiClient client=new GoogleApiClient.Builder(this).addApi(LocationServices.API).build();
        for (int i = 0; i<userDetail.size(); i++){
            int pendingId = Integer.parseInt(userDetail.get(i).get("id"));
            LocationServices.getGeofencingClient(getApplicationContext()).removeGeofences(getPendingIntent(pendingId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("GeoFenceHelper", "Successfully Removed geofence" + pendingId);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("GeoFenceHelper", "Failure " + e.getMessage());
                }
            });
        }
//        LocationServices.GeofencingApi.removeGeofences(
//                client,
//                getPendingIntent()
//// This is the s
//        );

    }

    @SuppressLint("MissingPermission")
    public void addAllGeofence(ArrayList<HashMap<String,String >> userDetail){
        geoFenceHelper = new GeoFenceHelper(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
        dbHelper = new DBHelper(this);
        for (int i=0; i<userDetail.size(); i++){
            List<Geofence> geofenceList = new ArrayList<>();
            String id = userDetail.get(i).get("id");
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
//                            if(errorMessage.equals("Geofence not avalible")){
//                                ArrayList<HashMap<String,String >> userDetail = dbHelper.GetUsers();
//                                addAllGeofence(userDetail);
//                            }
                        }
                    });
        }

    }

    public String getErrorString(Exception e){
        if(e instanceof ApiException){
            ApiException apiException=(ApiException)e;
            switch (apiException.getStatusCode()){
                case GeofenceStatusCodes
                        .GEOFENCE_NOT_AVAILABLE:
                    return "Geofence not avalible";
                case GeofenceStatusCodes
                        .GEOFENCE_TOO_MANY_GEOFENCES:
                    return "Too many Geofence";
                case GeofenceStatusCodes
                        .GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "Geofence Too many pending intent";
            }
        }
        return e.getLocalizedMessage();
    }
}
