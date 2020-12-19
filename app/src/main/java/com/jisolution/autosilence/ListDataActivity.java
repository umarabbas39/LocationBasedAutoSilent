package com.jisolution.autosilence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.jisolution.autosilence.DB.DBHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.Geofence;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ListDataActivity extends AppCompatActivity implements Dialog.DialogListner, SearchView.OnQueryTextListener {

    DBHelper db;
    GeoFenceHelper geoFenceHelper;
    ArrayList<HashMap<String, String>> userList;
    ArrayList<HashMap<String, String>> latlngList;
    ArrayList<String> mylist,mylat,mylng;
    HashMap<String, String> tempData ;
    ListView lv;
    SimpleAdapter adapter;
    MapsActivity mapsActivity;
    int p;
    AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data);
        db = new DBHelper(this);
        geoFenceHelper = new GeoFenceHelper(this);
        ad();
        userList = db.GetUsers();
        Log.d("Dialog", userList + "");
        tempData = new HashMap<String, String>();
//        for(int i=0;i<=userList.size();i++){
//           Long Time=Long.parseLong(userList.get(i).get("time"));
//            String name= userList.get(i).get("name");
//            String radius = userList.get(i).get("radius");
//            String id = userList.get(i).get("id");
//
//            db.UpdateUserDetails(name, String.valueOf(time1), id);
//
//        }
        mapsActivity=new MapsActivity();
        lv = (ListView) findViewById(R.id.list_view);
        // Code for stop watch
//        Thread thread = new Thread(){
//            @Override
//            public void run() {
//                while(true){
//                    ArrayList<HashMap<String, String>> newList = new ArrayList<HashMap<String, String>>();
//                    HashMap<String, String> map =null;
//
//                    for (int i = 0; i < userList.size(); i++) {
//                        map = new HashMap<String, String>();
//                        String name = userList.get(i).get("name");
//                        String radius = userList.get(i).get("radius");
//                        String date = userList.get(i).get("date");
//                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");
//                        String ago = "";
//                        try{
//                            Date mDate = sdf.parse(date);
//                            long timeInMilliseconds = mDate.getTime();
//                            long current = System.currentTimeMillis();
//                            ago = convertTimeInMilli(current - timeInMilliseconds);
//                            Log.d("ListDataActivity", ago);
//                        }catch(Exception e){
//                            Log.e("ListDataActivity", e.getMessage());
//                        }
//                        map.put("name", name);
//                        map.put("radius", radius);
//                        map.put("date", ago);
//                        newList.add(i, map);
//                    }
//                    adapter = new SimpleAdapter(ListDataActivity.this, newList, R.layout.list_row,new String[]{"name","radius","date"}, new int[]{R.id.name, R.id.radius, R.id.time});
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            lv.setAdapter(adapter);
//                        }
//                    });
//                    Log.d("ListDataActivity", newList + "");
//                    try{
//                        Thread.sleep(1000);
//                    }catch(Exception e){
//                        Log.e("ListDataActivity", e.getMessage());
//                    }
//                }
//            }
//
//        };
//        thread.start();
        adapter = new SimpleAdapter(ListDataActivity.this, userList, R.layout.list_row,new String[]{"name","radius","date"}, new int[]{R.id.name, R.id.radius, R.id.time});
        lv.setAdapter(adapter);

        mylist=getPropertyList(userList,"name");
        registerForContextMenu(lv);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.popup_menu,menu);

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info=(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch (item.getItemId()){
            case R.id.delete:
                int pendingId = db.getUserId(userList.get(info.position).get("name"));

                try {
                    geoFenceHelper.removeAllGeofence(userList);
                    db.DeleteUser(mylist.get(info.position));
                    userList.remove(info.position);
                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                    if(sharedPrefs.getBoolean("background", true)){
                        geoFenceHelper.addAllGeofence(userList);
                    }
//                    removeGeoFence(info.position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(),"Item Deleted",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.update:
                p=info.position;
                Log.e("position", "onContextItemSelected: "+p );
                Dialog dialog=new Dialog();
                dialog.show(getSupportFragmentManager(),"dialog");
                return true;

            default:
                return super.onContextItemSelected(item);
        }


    }

    private void removeGeoFence(int position) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = sharedPrefs.getString("Value", "");
        Type type = new TypeToken<List<Geofence>>() {}.getType();
        List<Geofence> arrayList = gson.fromJson(json, type);
        arrayList.remove(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_items, menu);

        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.
                getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);

        return true;
    }
    public static ArrayList<String> getPropertyList(ArrayList<HashMap<String,String>> users, String key)
    {
        ArrayList<String> result = new ArrayList<String>();
        for (HashMap<String,String> map : users) {
            result.add(map.get(key));
        }
        return result;
    }

    @Override
    public void applytext(String name, String radius) {
        String id=mylist.get(p);
//        mapsActivity.geofenceList.remove(p);
        latlngList=db.GetUserByUserId(id);
        mylat=getPropertyList(latlngList,"lat");
        mylng=getPropertyList(latlngList,"lng");
        db.UpdateDetails(name,radius,id);
        userList = db.GetUsers();
        adapter = new SimpleAdapter(ListDataActivity.this, userList, R.layout.list_row,new String[]{"name","radius","date"}, new int[]{R.id.name, R.id.radius, R.id.time});
        lv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Toast.makeText(getApplicationContext(),"Item Updated ",Toast.LENGTH_SHORT).show();
        Log.d("Dialog", latlngList + " ");
        int pendingId = db.getUserId(name);
        mapsActivity.updateGeofence(name,radius,mylat.get(0),mylng.get(0), pendingId);
//        Toast.makeText(getApplicationContext(),"Item Update",Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        adapter.getFilter().filter(s);
        return true;
    }
    private void ad() {
        mAdView = findViewById(R.id.adView);
        MobileAds.initialize(this,"ca-app-pub-4962376926519245/1463934372");
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

//    private String convertTimeInMilli(long millis){
//        String time = "";
//        long days = TimeUnit.MILLISECONDS.toDays(millis);
//        millis -= TimeUnit.DAYS.toMillis(days);
//        long hours = TimeUnit.MILLISECONDS.toHours(millis);
//        millis -= TimeUnit.HOURS.toMillis(hours);
//        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
//        millis -= TimeUnit.MINUTES.toMillis(minutes);
//        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
//        time = days + "d:" + hours + "h:" + minutes + "m:"+seconds+"s";
//        return time;
//    }
}