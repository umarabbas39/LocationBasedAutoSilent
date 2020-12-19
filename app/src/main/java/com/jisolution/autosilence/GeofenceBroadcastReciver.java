package com.jisolution.autosilence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.jisolution.autosilence.DB.DBHelper;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class GeofenceBroadcastReciver extends BroadcastReceiver {
    private static final String TAG = "GeofenceReciver";
    public Long timeSpent;
    DBHelper dbHelper;
    ArrayList<HashMap<String,String >> userDetail;
    Long time1;
    String name,radius,id;
    SharedPreferences sharedPreferences;
    boolean notification,vibration;
    Vibrator v;

    @Override
    public void onReceive(Context context, Intent intent) {

        AudioManager audioManager;
//        audioManager = (AudioManager)(Context.AUDIO_SERVICE);
        NotificationHelper notificationHelper=new NotificationHelper(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationHelper.createChannels("Notifications");
        }
        GeofencingEvent geofencingEvent=GeofencingEvent.fromIntent(intent);
        audioManager=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        dbHelper=new DBHelper(context);
        userDetail=dbHelper.GetUsers();
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        notification=sharedPreferences.getBoolean("noti", true);
        vibration=sharedPreferences.getBoolean("vibe", true);
        Log.e("NOTI", "onReceive: "+notification);
        Log.e("VIBE", "onReceive: "+vibration );
        Location location=geofencingEvent.getTriggeringLocation();
        String lat=String.valueOf(location.getLatitude());
        String lng=String.valueOf(location.getLongitude());
        if(userDetail.size()>0) {
            name= userDetail.get(userDetail.size() - 1).get("name");
//        String Time=userDetail.get(userDetail.size()-1).get("time");
            radius = userDetail.get(userDetail.size() - 1).get("radius");
            id = userDetail.get(userDetail.size() - 1).get("id");
        }
        else{
            name="";
            id="1";
        }

        if(geofencingEvent.hasError()){
            Toast.makeText(context, "Geofence has error", Toast.LENGTH_SHORT).show();
            Log.e(TAG,"OnRecive:Error recive geofence event...");
            return;
        }

        List<Geofence> geofenceslist=geofencingEvent.getTriggeringGeofences();
        //Location geofencelocation=geofencingEvent.getTriggeringLocation();

        for (Geofence geofence:geofenceslist){
            Log.d(TAG,"OnRecive:"+geofence.getRequestId());
        }

        int transtiontype=geofencingEvent.getGeofenceTransition();



        switch (transtiontype){
            case Geofence.GEOFENCE_TRANSITION_ENTER:

                Log.d("GeofenceReciver", "onReceive: Entered" );
                 time1=System.currentTimeMillis();
                dbHelper.UpdateUserDetails(name, String.valueOf(time1), id);
                if(notification) {
                    notificationHelper.sendHighPriorityNotification("Mobile Switch to Silent Mode", "", MapsActivity.class);
                }else if(vibration) {
                    Log.d("type", "onReceive: vibration checked");

// Vibrate for 500 milliseconds
                    if (v.hasVibrator()) {
                        Log.e("vibration", "onReceive: has vibrator" );
                        long[] pattern = {0, 100, 500, 100, 500, 100,500, 100,500, 100,500};
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK),
                                    new AudioAttributes.Builder()
                                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                            .setUsage(AudioAttributes.USAGE_ALARM)
                                            .build());
                        } else {
                            v.vibrate(new long[]{0,2* DateUtils.SECOND_IN_MILLIS}, 0);
                           Handler handler=new Handler();
                           handler.postDelayed(new Runnable() {
                               @Override
                               public void run() {
                                   v.cancel();
                               }
                           }, 2000);
                        }
                    }
                }
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
//                Toast toast = Toast.makeText(context,
//                        String.valueOf("Timer has started with "+timeSpent),
//                        Toast.LENGTH_SHORT);
//
//                toast.show();

                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Long time2=System.currentTimeMillis();
                Log.d("GeofenceReciver", "onReceive: Exited" );
                Long Time= Long.valueOf(userDetail.get(userDetail.size()-1).get("time"));
               timeSpent=(time2-Time)/1000;
                Log.e("timeSpent", "onReceive: "+timeSpent );
               int h=0,m=0,s=0,c=0;
               for(long i=0;i<=timeSpent;i++){
                   c++;
                   if(c>=60){
                       m++;
                       c=0;
                       if(m>=60){
                           h++;
                           m=0;
                       }
                   }
                   s=c;
                   Log.e("timediff", "onReceive: "+h+m+s );
               }
               String date=DateFormat.getDateTimeInstance().format(new Date());
//                dbHelper.insertUserDetails(name, radius, String.valueOf(time2),date,lat , lng);
                dbHelper.UpdateUserDetails(name,String.valueOf(time2),id);
               String body="You spent "+h+" hours "+m+" minutes "+s+" seconds at "+name;
               if(notification){
                notificationHelper.sendHighPriorityNotification("Mobile Switch to Normal mode",body,MapsActivity.class);
               }else if(vibration){
                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                       v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                   } else {
                       //deprecated in API 26
                       v.vibrate(500);
                   }
               }
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                break;
//            case Geofence.GEOFENCE_TRANSITION_DWELL:
//                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);     audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
//                notificationHelper.sendHighPriorityNotification("Enter Dewell","",MapsActivity.class);
//                break;
        }
    }
}
