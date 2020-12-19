package com.jisolution.autosilence;

import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceService extends JobIntentService {

    private static final String TAG = "GeofenceBroadcastRecive";

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        NotificationHelper notificationHelper=new NotificationHelper(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationHelper.createChannels("Notifications");
        }
        GeofencingEvent geofencingEvent=GeofencingEvent.fromIntent(intent);

        if(geofencingEvent.hasError()){
            Toast.makeText(getApplicationContext(), "Geofence has error", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"OnRecive:Error recive geofence event...");
        }

        List<Geofence> geofenceslist=geofencingEvent.getTriggeringGeofences();

        for (Geofence geofence:geofenceslist){
            Log.d(TAG,"OnRecive:"+geofence.getRequestId());
        }

        int transtiontype=geofencingEvent.getGeofenceTransition();

        switch (transtiontype){
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                notificationHelper.sendHighPriorityNotification("Enter geofence","",MapsActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                notificationHelper.sendHighPriorityNotification("Exit geofence","",MapsActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                notificationHelper.sendHighPriorityNotification("Enter Dewell","",MapsActivity.class);
                break;
        }
    }

}
