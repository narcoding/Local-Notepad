package com.narcoding.localnotepad.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.narcoding.localnotepad.DBHelper;
import com.narcoding.localnotepad.Item;
import com.narcoding.localnotepad.R;

import java.util.ArrayList;
import java.util.Collections;

public class NotesMap extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap_Notes;
    private Cursor notes;
    DBHelper dbHelper;

    // items contain notes titles
    private ArrayList<String> titles;
    private ArrayList<Item> items;
    private ArrayList<String> locations;
    private ArrayList<Marker> markers;
    private ArrayList<Double> latitude;
    private ArrayList<Double> longitude;


    // variable will contain the position of clicked item in listview
    private int position = 0;
    int c=0;

    boolean isGPSEnabled;
    CameraUpdate cu;
    LatLngBounds bounds;

    private ImageButton imgBtn_allmarkers;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_map);

        dbHelper = new DBHelper(getApplicationContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Map_Notes);
        mapFragment.getMapAsync(this);

        setNotes();
        imgBtn_allmarkers= (ImageButton) findViewById(R.id.imgBtn_allmarkers);
        imgBtn_allmarkers.setAlpha(0.8f);
        imgBtn_allmarkers.setBackgroundColor(Color.WHITE);

    }

    private void gpsOpen(){

        LocationManager lm = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled= lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPSEnabled) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(NotesMap.this);
            final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
            final String message = "Your GPS seems to be disabled, do you want to enable it?";

            builder.setMessage(message)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    NotesMap.this.startActivity(new Intent(action));
                                    d.dismiss();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                    Toast.makeText(NotesMap.this,"GPS don't open!",Toast.LENGTH_SHORT).show();
                                }
                            });
            builder.create().show();

        }
        else {
            Toast.makeText(this, "GPS is active now!", Toast.LENGTH_SHORT).show();
        }

    }




    public void setNotes() {
        // init the items arrayList
        titles = new ArrayList<String>();
        items = new ArrayList<Item>();
        locations = new ArrayList<String>();
        latitude=new ArrayList<Double>();
        longitude=new ArrayList<Double>();
        markers = new ArrayList<Marker>();

        // getting readable database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // getting notes from db
        // see dbhelper for more details
        notes = dbHelper.getNotes2(db);


        // this should fix the problem
        // now the activity will be managing the cursor lifecycle
        startManagingCursor(notes);

        // closing database connection !important
        // always close connection with database
        // we closing database connection here because we don't use db anymore
        db.close();

        // populating ArrayList items with notes titles
        if (notes.moveToFirst()) {
            do {
                items.add(new Item(notes.getShort(0), notes.getString(1), notes.getString(2),notes.getString(3)));
            } while (notes.moveToNext());
        }


        for (Item i : items) {
            titles.add(i.getTitle());
        }

        locations.removeAll(Collections.singleton(null));
        for (Item i : items) {
            locations.add(i.getLocation());
        }


        for (int a = 0; a < locations.size(); a++)
            {
                    String[] latlong = locations.get(a).split("/");

                    latitude.add(Double.parseDouble(latlong[0]));
                    longitude.add(Double.parseDouble(latlong[1]));

            }

            stopManagingCursor(notes);

        }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap_Notes = googleMap;
        googleMap.setOnInfoWindowClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap_Notes.setMyLocationEnabled(true);


        setNotes();

        for (int a = 0; a < locations.size(); a++) {

                markers.add(mMap_Notes.addMarker(new MarkerOptions().position(new LatLng(latitude.get(a), longitude.get(a)))));



        }

            int b=0;
        for (Item i : items) {

            if(b<markers.size()) {
                
                markers.get(b).setTitle(i.getTitle());
                b = b + 1;

            }
        }








        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {

            builder.include(marker.getPosition());
        }
        bounds = builder.build();
        int padding = 80; // offset from edges of the map in pixels
        cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap_Notes.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                //Your code where exception occurs goes here...
                mMap_Notes.animateCamera(cu);
            }
        });



        gpsOpen();


        //mMap_Notes.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

        //    @Override

        //    public void onMyLocationChange(Location location) {

                //

        //        float zoomLevel = (float) 16.0; //This goes up to 21

        //        mMap_Notes.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoomLevel));

                //

        //    }

        //});



        imgBtn_allmarkers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mMap_Notes.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        //Your code where exception occurs goes here...
                        mMap_Notes.animateCamera(cu);
                    }
                });

            }
        });




    }



    @Override
    public void onInfoWindowClick(Marker marker) {
        //String title = marker.getTitle().toString();
        //Intent mIntent = new Intent(this, OneNote.class);
        //mIntent.putExtra("id", marker.getId());
        //startActivity(mIntent);
    }
}

