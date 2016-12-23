package com.narcoding.localnotepad.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.narcoding.localnotepad.DBHelper;
import com.narcoding.localnotepad.R;

import java.io.ByteArrayOutputStream;

public class OneNote extends ActionBarActivity implements OnMapReadyCallback {

    private GoogleMap mMap_OneNote;

    private static final String TAG = "notepad";

    //our views for display note title and content
    private TextView noteTitle;
    private TextView createdAt;
    private TextView noteContent;
    private ImageView img_showNoteImage,img_voiceplay;

    //dbhelper
    private DBHelper dbhelper;
    private SQLiteDatabase db;

    //default values for title and content variables
    private String title = "defaultTitle";
    private int id = 0;
    private String content = "defaultContent";
    private String date = "date";
    private String location="location";
    private byte[] image;
    private byte[] voice;


    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_note);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_OneNote);
        mapFragment.getMapAsync(this);

        //initialization of DBHelper
        dbhelper = new DBHelper(getApplicationContext());


        noteTitle = (TextView) findViewById(R.id.noteTitle);
        noteContent = (TextView) findViewById(R.id.noteContent);
        createdAt = (TextView) findViewById(R.id.createdAt);
        img_showNoteImage= (ImageView) findViewById(R.id.img_showNoteImage);
        img_voiceplay= (ImageView) findViewById(R.id.img_voiceplay);

        id = getIntent().getIntExtra("id", 0);

        //getting the readable database
        db = dbhelper.getReadableDatabase();

        //getting the note from database
        Cursor c = dbhelper.getNote(db, id);
        //closing the database connection
        db.close();

        //getting the content from cursor
        //getString(1) because first column is noteTitle and second is noteContent and the third column is date
        title = c.getString(0).toString();
        content = c.getString(1).toString();
        date = c.getString(2).toString();
        location=c.getString(3).toString();
        image=c.getBlob(4);
        voice=c.getBlob(5);



        if(!location.equals("null")) {
            String[] latlong = location.split("/");
            latitude = Double.parseDouble(latlong[0]);
            longitude = Double.parseDouble(latlong[1]);

        }


        //setting notes to our viewssss
        noteTitle.setText(title);
        noteContent.setText(content);
        createdAt.setText(date);
        if(image!=null) {
            final Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            img_showNoteImage.setImageBitmap(bitmap);

            img_showNoteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    //bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    //byte[] byteArray = stream.toByteArray();

                    Intent in1 = new Intent(OneNote.this, FullImageActivity.class);
                    in1.putExtra("image",image);

                    startActivity(in1);
                }
            });

        }

        if(voice!=null){

            img_voiceplay.setBackgroundResource(R.drawable.ic_media_play);

            img_voiceplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intentvoice=new Intent(OneNote.this,VoiceRecordActivity.class);
                    intentvoice.putExtra("voice",voice);
                    intentvoice.putExtra("fromthere","OneNote");
                    startActivity(intentvoice);

                }
            });

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap_OneNote = googleMap;



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
        mMap_OneNote.setMyLocationEnabled(true);

        //LatLngBounds.Builder builder = new LatLngBounds.Builder();
        //for (Marker marker : markers) {
        //    builder.include(marker.getPosition());
        //}
        //LatLngBounds bounds = builder.build();
        //int padding = 0; // offset from edges of the map in pixels
        //CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        if(!location.equals("null")) {
            float zoomLevel = (float) 16.0; //This goes up to 21
            mMap_OneNote.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoomLevel));
            mMap_OneNote.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(title)).showInfoWindow();


        }

    }


}
