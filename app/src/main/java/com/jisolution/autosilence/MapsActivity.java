package com.jisolution.autosilence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.jisolution.autosilence.DB.DBHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, Dialog.DialogListner, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MapsActivity";
    GoogleMap mMap;
    GeofencingClient geofencingClient;
    int Fine_Location_Request_Code = 1001;
    int Background_Access_Location_Request_Code = 1002;
    int geofence_radius = 50;//hardcode but latter to be set
    GeoFenceHelper geoFenceHelper;
    String geofence_ID = "Set_Geofence_ID";//hardcoded just for one id
    Button save;
    ImageButton showlocation, setting;
    LatLng latLngs;
    public List<Geofence> geofenceList = new ArrayList<>();
    DBHelper dbHelper;
    AdView mAdView;
    InterstitialAd mInterstitialAd;
    GoogleApiClient client;
    //    GeofenceBackground background;
    ArrayList<HashMap<String, String>> data;
    boolean repeat = false;
    boolean isRepeat = false;
    LatLng currentLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        intial();
//        Intent i = new Intent(this, ForegroundService.class);
//        this.startService(i);

//        if (!Places.isInitialized()) {
//            Places.initialize(getApplicationContext(), "AIzaSyD_pS9r0m4cYNwqpDJDEP4VgZdo8whRPs0");
//        }
//        PlacesClient placesCLient = Places.createClient(this);
//        // Initialize the AutocompleteSupportFragment.
//        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
//                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
//
//        // Specify the types of place data to return.
//        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
//
//
//        // Set up a PlaceSelectionListener to handle the response.
//        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override
//            public void onPlaceSelected(@NotNull Place place) {
//                // TODO: Get info about the selected place.
//                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
//            }
//            @Override
//            public void onError(@NotNull Status status) {
//                // TODO: Handle the error.
//                Log.i(TAG, "An error occurred: " + status);
//            }
//        });


        ad();
//        startService(new Intent(this, GeofenceService.class));
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getid();


            }


            private void getid() {


                openDialog();

                // Toast.makeText(getApplicationContext(),""+geofence_ID,Toast.LENGTH_SHORT).show();


            }
        });
        showlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, ListDataActivity.class);
                startActivity(intent);
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, SettingActivity.class);
                startActivity(intent);

            }
        });

        client = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addOnConnectionFailedListener(this).enableAutoManage(this, this).build();
//        background=new GeofenceBackground(getApplicationContext(), client);
//        background.registerAllGeofences();

    }


    private void openDialog() {
        Dialog dialog = new Dialog();
        dialog.show(getSupportFragmentManager(), "dialog");


    }

    private void intial() {
        geofencingClient = LocationServices.getGeofencingClient(this);
        geoFenceHelper = new GeoFenceHelper(this);
        save = findViewById(R.id.save);
        showlocation = findViewById(R.id.show_location);
        setting = findViewById(R.id.settings);
        dbHelper = new DBHelper(this);
        ArrayList<HashMap<String, String>> list = dbHelper.GetUsers();
//        geoFenceHelper.addAllGeofence(list);
        Log.d("MapsActivity", list + "");
        if (list.size() > 0) {
            WorkRequest registerWorkRequest = new PeriodicWorkRequest.Builder(GeofenceRegisterWorker.class, 30, TimeUnit.MINUTES).addTag("worker").build();
            WorkManager.getInstance(this).enqueue(registerWorkRequest);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableUserLocation();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location gps_loc;
        Location network_loc;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }else{
            gps_loc = null;
            network_loc = null;
        }

        LatLng currentLocation;
        if (gps_loc != null) {
            currentLocation = new LatLng(gps_loc.getLatitude(), gps_loc.getLongitude());
        }
        else if(network_loc != null) {
            currentLocation = new LatLng(network_loc.getLatitude(), network_loc.getLongitude());
        }else{
            currentLocation = new LatLng(33.6662, 73.1055);
        }

        // Add a marker in Sydney and move the camera
//        LatLng islamabad = new LatLng(33.6662, 73.1055);
        // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Pakistan"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));

        mMap.setOnMapClickListener(this);
    }

    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            //ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Dialog
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Fine_Location_Request_Code);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Fine_Location_Request_Code);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Fine_Location_Request_Code) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //premission granted
                mMap.setMyLocationEnabled(true);
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Location network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (gps_loc != null) {
                    currentLocation = new LatLng(gps_loc.getLatitude(), gps_loc.getLongitude());
                }
                else if(network_loc != null) {
                    currentLocation = new LatLng(network_loc.getLatitude(), network_loc.getLongitude());
                }else{
                    currentLocation = new LatLng(33.6662, 73.1055);
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
                if(isRepeat==false) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MapsActivity.this);
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
                    isRepeat=true;
                }
            } else {
                //permission do not granted
            }
        }
        if (requestCode == Background_Access_Location_Request_Code) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //premission granted
                Toast.makeText(this, "You can add Geofences", Toast.LENGTH_SHORT).show();
                if(isRepeat==false) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MapsActivity.this);
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
                    isRepeat = true;
                }
            } else {
                //permission do not granted
                Toast.makeText(this, "Background Location Access is Required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {

        if (Build.VERSION.SDK_INT >= 29) {
            //background premission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                handleMaplongclick(latLng);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    //dialog
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.
                            ACCESS_BACKGROUND_LOCATION}, Background_Access_Location_Request_Code);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.
                            ACCESS_BACKGROUND_LOCATION}, Background_Access_Location_Request_Code);
                }
            }


        } else {
            handleMaplongclick(latLng);
        }

    }

    private void handleMaplongclick(LatLng latLng) {
        mMap.clear();//to clear all previous marker
        addMarker(latLng);
        addCircle(latLng, 50);
        latLngs = latLng;
//        addGeofence(latLng, 100);
    }

    @SuppressLint("MissingPermission")
    private void addGeofence(LatLng latLng, float radius, int id) {
        Geofence geofence = geoFenceHelper.getGeofence(geofence_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER |
                Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        geofenceList.add(geofence);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Gson gson = new Gson();

        String json = gson.toJson(geofenceList);

        editor.putString("Value", json);
        editor.apply();

        if(sharedPrefs.getBoolean("background", true)){
            GeofencingRequest geofencingRequest = geoFenceHelper.getGeofenceRequest(geofenceList);
            PendingIntent pendingIntent = geoFenceHelper.getPendingIntent(id);
            // pendingIntent.notify();
            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Success:Geofenceadded" + id);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String errorMessage = geoFenceHelper.getErrorString(e);
                            Log.e(TAG, "onFaliure" + errorMessage);

                        }
                    });
        }

    }


    public void updateGeofence(String id, String r, String lat, String lng, int pendingId) {

        geofence_ID = id;
        float radius = Float.parseFloat(r);
        double lat_dob = Double.parseDouble(lat);
        double lng_dob = Double.parseDouble(lng);
        LatLng latLng = new LatLng(lat_dob, lng_dob);


        Geofence geofence = geoFenceHelper.getGeofence(geofence_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER |
                Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        geofenceList.add(geofence);
        GeofencingRequest geofencingRequest = geoFenceHelper.getGeofenceRequest(geofenceList);
        PendingIntent pendingIntent = geoFenceHelper.getPendingIntent(pendingId);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPrefs.getBoolean("background", true)){
            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Success:Geofenceadded");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            String errorMessage = geoFenceHelper.getErrorString(e);

                            Log.d(TAG, "onFaliure" + errorMessage);

                        }
                    });
        }
    }


    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);
    }

    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);

    }

    @Override
    public void applytext(String name, String radius) {
        try {
            repeat = false;
            data = dbHelper.GetUsers();
            for (int i = 0; i < data.size(); i++) {
                String locationName = data.get(i).get("name");
                if (locationName.equals(name)) {
                    repeat = true;
                }
            }

            if (!repeat) {
                geofence_ID = name;
                try {
                    geofence_radius = Integer.parseInt(radius);
                    long time = System.currentTimeMillis();
                    String date = DateFormat.getDateTimeInstance().format(new Date());
                    dbHelper.insertUserDetails(name, radius, String.valueOf(time), date, String.valueOf(latLngs.latitude), String.valueOf(latLngs.longitude));

                    int intentId = dbHelper.getUserId(name);
                    addGeofence(latLngs, geofence_radius, intentId);
                    Toast.makeText(getApplicationContext(), intentId + " saved.", Toast.LENGTH_SHORT).show();
//                    background.updateGeofencesList(geofence_ID, latLngs, geofence_radius);
//                    background.registerAllGeofences();


                } catch (NumberFormatException nfe) {
                    Toast.makeText(this, "Enter Number", Toast.LENGTH_SHORT).show();
                    System.out.println("Could not parse " + nfe);
                }
            } else {
                Toast.makeText(geoFenceHelper, "A location with the same name is already saved.", Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"Please Select Location First",Toast.LENGTH_SHORT).show();
        }
    }

    private void ad() {
        mAdView = findViewById(R.id.adView);
        MobileAds.initialize(this,"ca-app-pub-4962376926519245/1463934372");
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-4962376926519245/2152337699");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!geofenceList.isEmpty()) {
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("GoogleApi", "onConnectionFailed: " );
    }


}