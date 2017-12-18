package com.astralbody888.alexanderconner.memorableplacesapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;
    private static DecimalFormat df2 = new DecimalFormat(".##");

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                centerMapOnLocation(lastKnownLocation, "Your Location");
            }
        }
    }

    public void centerMapOnLocation(Location location, String title) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();

        if (title != "Your location")
        {
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        }
        else {
           // mMap.addMarker(new MarkerOptions().position(userLocation).title(title).icon(""));
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        //calls from the intent that got us here
        Intent intent = getIntent();
        //If the list item clicked was the 0th Add a New Place item
        if (intent.getIntExtra("placeNumber", 0) == 0) {
            //Zoom in on users location
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerMapOnLocation(location, "Your location");
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };
            // if build is before marshmellow, go ahead and run the updates
            if (Build.VERSION.SDK_INT < 23) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    //We have permission so get updates
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    centerMapOnLocation(lastKnownLocation, "Your Location");
                } else {
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        //If the list item clicked was  a previously saved location
        }else {
            Location placeL = new Location(LocationManager.GPS_PROVIDER);
            LatLng ll = MainActivity.locations.get(intent.getIntExtra("placeNumber", 0));
            placeL.setLatitude(ll.latitude);
            placeL.setLongitude(ll.longitude);
            String a = MainActivity.places.get(intent.getIntExtra("placeNumber", 0));
            centerMapOnLocation(placeL, a);
        }

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        //Geocoder is used to get the address of the location that was tapped
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";

        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addressList != null && addressList.size() > 0) {
                if (addressList.get(0).getThoroughfare() != null)
                {
                    if (addressList.get(0).getSubThoroughfare() != null)
                    {
                        address += addressList.get(0).getSubThoroughfare() + " ";
                    }
                    address += addressList.get(0).getThoroughfare();
                } else
                {

                    address += addressList.get(0).getLatitude() + ", " + addressList.get(0).getLongitude() + " " + addressList.get(0).getCountryName();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (address == "") {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm MM-dd-yyyy");
            address = sdf.format(new Date());
        }

        mMap.addMarker(new MarkerOptions().position(latLng).title(address));
        MainActivity.places.add(address);
        MainActivity.locations.add(latLng);
        //Update ArrayList view
        MainActivity.arrayAdapter.notifyDataSetChanged();

        SharedPreferences userPreferences = this.getSharedPreferences("com.astralbody888.alexanderconner.memorableplacesapp", Context.MODE_PRIVATE);

        try {
            //We can only save strings to the users device, so we need to convert LatLngs to coordinates
            //Then when we have arrayLists of strings we can serialize them into strings with objectserializer
            ArrayList<String> latitudes = new ArrayList<>();
            ArrayList<String> longitudes = new ArrayList<>();

            for (LatLng coordinates : MainActivity.locations) {
                latitudes.add(Double.toString(coordinates.latitude));
                longitudes.add(Double.toString(coordinates.longitude));
            }

            userPreferences.edit().putString("places", ObjectSerializer.serialize(MainActivity.places)).apply();
            userPreferences.edit().putString("latitudes", ObjectSerializer.serialize(latitudes)).apply();
            userPreferences.edit().putString("longitudes", ObjectSerializer.serialize(longitudes)).apply();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Location Saved", Toast.LENGTH_SHORT).show();

    }
}
