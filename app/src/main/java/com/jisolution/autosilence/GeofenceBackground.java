package com.jisolution.autosilence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class GeofenceBackground {
//    public static final String TAG = GeofenceBackground.class.getSimpleName();
//
//    private List<Geofence> mGeofenceList;
//    private PendingIntent mGeofencePendingIntent;
//    private GoogleApiClient mGoogleApiClient;
//    private Context mContext;
//
//    public GeofenceBackground(Context context, GoogleApiClient client) {
//        mContext = context;
//        mGoogleApiClient = client;
//        mGeofencePendingIntent = null;
//        mGeofenceList = new ArrayList<>();
//    }
//
//    public void registerAllGeofences() {
//        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected() ||
//                mGeofenceList == null || mGeofenceList.size() == 0) {
//            return;
//        }
//        try {
//            LocationServices.GeofencingApi.addGeofences(
//                    mGoogleApiClient,
//                    getGeofencingRequest(),
//                    getGeofencePendingIntent()
//            );
//        } catch (SecurityException securityException) {
//
//            Log.e(TAG, securityException.getMessage());
//        }
//    }
//
//    public void unRegisterAllGeofences() {
//        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
//            return;
//        }
//        try {
//            LocationServices.GeofencingApi.removeGeofences(
//                    mGoogleApiClient,
//                    getGeofencePendingIntent()
//            );
//        } catch (SecurityException securityException) {
//            Log.e(TAG, securityException.getMessage());
//        }
//    }
//
//
//    public void updateGeofencesList(String ID, LatLng latLng, float radius) {
//        mGeofenceList = new ArrayList<>();
//
//            Geofence geofence = new Geofence.Builder()
//                    .setRequestId(ID)
//                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
//                    .setCircularRegion(latLng.latitude, latLng.latitude, radius)
//                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
//                    .build();
//            mGeofenceList.add(geofence);
//        }
//
//    private GeofencingRequest getGeofencingRequest() {
//        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
//        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
//        builder.addGeofences(mGeofenceList);
//        return builder.build();
//    }
//
////    private PendingIntent getGeofencePendingIntent() {
////        // Reuse the PendingIntent if we already have it.
////        if (mGeofencePendingIntent != null) {
////            return mGeofencePendingIntent;
////        }
////        Intent intent = new Intent(mContext, GeofenceBroadcastReciver.class);
////        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 2607, intent, PendingIntent.
////                FLAG_UPDATE_CURRENT);
////        return mGeofencePendingIntent;
////    }

}
