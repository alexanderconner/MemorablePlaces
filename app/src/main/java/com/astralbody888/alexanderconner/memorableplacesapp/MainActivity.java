package com.astralbody888.alexanderconner.memorableplacesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //Make these arrays static so they can accessed from any activity.
    static ArrayList<String> places = new ArrayList<>();
    static ArrayList<LatLng> locations = new ArrayList<>();
    static ArrayAdapter arrayAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.listView);

        //SharedPreferences is for saving user data on their device.
        SharedPreferences userPreferences = this.getSharedPreferences("com.astralbody888.alexanderconner.memorableplacesapp", Context.MODE_PRIVATE);

        //SharedPreferences can only save strings, so our LatLngs for locations need to be saved as
        //ArrayLists of strings which we then Serialize to save, and Deserialize to read and use.
        ArrayList<String> latitudes = new ArrayList<>();
        ArrayList<String> longitudes = new ArrayList<>();

        //Refresh data
        places.clear();
        latitudes.clear();
        longitudes.clear();
        locations.clear();

        try {
            places = (ArrayList<String>) ObjectSerializer.deserialize(userPreferences.getString("places", ObjectSerializer.serialize(new ArrayList<String>())));
            latitudes = (ArrayList<String>) ObjectSerializer.deserialize(userPreferences.getString("latitudes", ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes = (ArrayList<String>) ObjectSerializer.deserialize(userPreferences.getString("longitudes", ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Check if data exists in users device prefs
        if (places.size() > 0 && latitudes.size() > 0 && longitudes.size() > 0)
        {
            //make sure theres a pair of latlngs for each place name
            if (places.size() == latitudes.size() && latitudes.size() == longitudes.size()) {

                for (int i = 0; i < latitudes.size(); i ++) {
                    //Recreate the saved locations
                    locations.add(new LatLng(Double.parseDouble(latitudes.get(i)), Double.parseDouble(longitudes.get(i))));

                }
            } else {
                Log.e("Data management error", "Something has gone horribly wrong with the users data.");
            }

        } else{
            //if no data, we create a default list item to prompt user to add new location
            places.add("Add a new place...");
            locations.add(new LatLng(0, 0));
        }


        //ArrayAdapter lets us add the ArrayList to the listView and display it
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, places);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                //Pass the placenumber clicked to the map activity.
                //The placenumber will be used to find the index of the static location and place so the
                //map will know what to display.
                intent.putExtra("placeNumber", i);

                startActivity(intent);

            }
        });

    }
}
