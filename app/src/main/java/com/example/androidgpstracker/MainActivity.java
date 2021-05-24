package com.example.androidgpstracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.androidgpstracker.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final int PERMISSION_FINE_LOCATION = 100;
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor,
            tv_updates, tv_address, tv_waypointCounts;
    Button btn_newWayPoint, btn_showWaypointList, btn_showMap;
    Switch sw_locationupdates, sw_gps;

    boolean updateOn = false; // Checks if tracking is enabled

    Location currentLocation; // Current Location

    List<Location> savedLocations;

    LocationRequest locationRequest; // Config file that stores settings for FusedLocationProvider

    LocationCallback locationCallBack;

    FusedLocationProviderClient fusedLocationProviderClient; //Location Service API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_speed);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        tv_waypointCounts = findViewById(R.id.tv_countOfWaypoints);

        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        sw_gps = findViewById(R.id.sw_gps);

        btn_newWayPoint = findViewById(R.id.btn_newWayPoint);
        btn_showWaypointList = findViewById(R.id.btn_showWaypointList);

        btn_showMap = findViewById(R.id.btn_showMap);

        //Location Request setup to set up location options
        locationRequest = new LocationRequest();
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                updateUIValues(location);
            }
        };

        btn_newWayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get gps
                MyApplication myApplication = (MyApplication)getApplicationContext();
                savedLocations = myApplication.getMyLocations();
                savedLocations.add(currentLocation);

            }
        });

        btn_showMap.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v)
           {
                Intent i = new Intent(MainActivity.this,MapsActivity.class);
                startActivity(i);
           }

        });


        btn_showWaypointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(MainActivity.this, ShowSavedLocationsList.class);
                startActivity(i);
            }

        });



        sw_gps.setOnClickListener(new View.OnClickListener()
        {
           @Override
           public void onClick(View v){
               if(sw_gps.isChecked())
               {
                   //checks if gps button is on
                   locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                   tv_sensor.setText("Using GPS Sensor");
                   }
               else {
                   locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                   tv_sensor.setText("Using Towers & WIFI");
               }

           }
        });
        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sw_locationupdates.isChecked()) {
                    // location tracking enabled
                    startLocationUpdates();
                }
                else {
                    stopLocationUpdates();
                }
            }
        });



        updateGPS();
    }

    private void startLocationUpdates() {
        tv_updates.setText("Location is being tracked");
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallBack,null);
        updateGPS();
    }

    private void stopLocationUpdates() {
        tv_updates.setText("Location is not being tracked");
        tv_lat.setText("Not tracking location");
        tv_lon.setText("Not tracking location");
        tv_speed.setText("Not tracking location");
        tv_altitude.setText("Not tracking location");
        tv_address.setText("Not tracking location");
        tv_accuracy.setText("Not tracking location");
        tv_sensor.setText("Not tracking location");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case PERMISSION_FINE_LOCATION:
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                updateGPS();
            }
            else
            {
                Toast.makeText(this,"This app requires permission", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        }
    }

    private void updateGPS()
    {
        //GPS Permission request
        // Get location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            // permission granted
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateUIValues(location);
                    currentLocation = location;



                }
            });
        }
        else {
            // permissions not granted
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_FINE_LOCATION);
            }
        }
    }

    private void updateUIValues(Location location) {
        //Text view object update
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if(location.hasAltitude()) {
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else {
            tv_altitude.setText("Not available on this device");
        }
        if(location.hasSpeed()) {
            tv_speed.setText(String.valueOf(location.getAltitude()));
        }
        else {
            tv_speed.setText("Not available on this device");
        }

        Geocoder geocoder = new Geocoder(MainActivity.this);
        try{
            List<Address> address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(address.get(0).getAddressLine(0));

        }
        catch(Exception e) {
            tv_address.setText("Cannot find street address");
        }

        MyApplication myApplication = (MyApplication)getApplicationContext();
        savedLocations = myApplication.getMyLocations();

        tv_waypointCounts.setText(Integer.toString(savedLocations.size()));

    }

}