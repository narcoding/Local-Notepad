package com.narcoding.localnotepad;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Map;

public class NotesMap extends AppCompatActivity implements OnMapReadyCallback {

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
    private Map<Marker,String> mark;


    // variable will contain the position of clicked item in listview
    private int position = 0;



    //double latitude;
    //double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_map);

        dbHelper = new DBHelper(getApplicationContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Map_Notes);
        mapFragment.getMapAsync(this);


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
                items.add(new Item(notes.getShort(0), notes.getString(1), notes.getString(2)));
            } while (notes.moveToNext());
        }


        for (Item i : items) {
            titles.add(i.getTitle());
        }

        for (Item i : items) {
            locations.add(i.getLocation());

        }




            getLocationsSize();

        for (int a = 0; a < locations.size(); a++) {
            {
                String[] latlong = locations.get(a).split("/");

                latitude.add(Double.parseDouble(latlong[0]));
                longitude.add(Double.parseDouble(latlong[1]));

            }

            stopManagingCursor(notes);

        }
    }

    private int getLocationsSize(){
    if(locations == null || locations.size()>0)
            return locations.size();
    else
            return 0;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap_Notes = googleMap;

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


        getLocationsSize();

        for (int a = 0; a < locations.size(); a++) {
                markers.add(mMap_Notes.addMarker(new MarkerOptions().position(new LatLng(latitude.get(a), longitude.get(a)))));

            }

            int b=0;
        for (Item i : items) {

                markers.get(b).setTitle(i.getTitle());
                b=b+1;

        }


        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 0; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap_Notes.animateCamera(cu);




    }

    }

