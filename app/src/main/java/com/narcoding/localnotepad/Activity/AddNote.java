package com.narcoding.localnotepad.Activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.narcoding.localnotepad.DBHelper;
import com.narcoding.localnotepad.R;

public class AddNote extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btn_confirm, btn_cancel;
    private ToggleButton tgBtn_UpdateLocation;
    private EditText titleEditText;
    private EditText contentEditText;
    private Location newLocation;

    private static final String TAG = "notepad";


    //this variable will inform us if user want to create a new note or just update
    private boolean isEdit;


    private DBHelper dbhelper;
    private SQLiteDatabase db;

    //variable will contain the title of editing note
    private String editTitle;
    private String strLocation;
    private String strNewLocation;
    private int id = 0;

    double lat;
    double lng;

    boolean isGPSEnabled;


    private void init() {
        btn_confirm = (Button) findViewById(R.id.btn_confirm);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        tgBtn_UpdateLocation = (ToggleButton) findViewById(R.id.tgBtnUpdateLocation);
        tgBtn_UpdateLocation.setAlpha(0.8f);
        tgBtn_UpdateLocation.setBackgroundColor(Color.WHITE);
        titleEditText = (EditText) findViewById(R.id.TitleEditText);
        contentEditText = (EditText) findViewById(R.id.ContentEditText);

        tgBtn_UpdateLocation.setText(getResources().getString(R.string.tgButtonOFF));
// Sets the text for when the button is first created.

        tgBtn_UpdateLocation.setTextOff(getResources().getString(R.string.tgButtonOFF));
// Sets the text for when the button is not in the checked state.

        tgBtn_UpdateLocation.setTextOn(getResources().getString(R.string.tgButtonON));

    }

    private void gpsOpen() {

        LocationManager lm = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPSEnabled) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(AddNote.this);
            final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
            final String message = getResources().getString(R.string.gpsSeemsDisable);

            builder.setMessage(message)
                    .setPositiveButton(getResources().getString(R.string.okey),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    AddNote.this.startActivity(new Intent(action));
                                    d.dismiss();
                                }
                            })
                    .setNegativeButton(getResources().getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                    Intent iBack = new Intent(AddNote.this, MainActivity.class);
                                    startActivity(iBack);
                                }
                            });
            builder.create().show();

        } else {
            Toast.makeText(this, getResources().getString(R.string.gpsActiveNow), Toast.LENGTH_SHORT).show();
        }

    }

    private void gpsOpenUpdate() {

        LocationManager lm = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPSEnabled) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(AddNote.this);
            final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
            final String message = getResources().getString(R.string.gpsSeemsDisable);

            builder.setMessage(message)
                    .setPositiveButton(getResources().getString(R.string.okey),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    AddNote.this.startActivity(new Intent(action));
                                    d.dismiss();
                                }
                            })
                    .setNegativeButton(getResources().getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                }
                            });
            builder.create().show();

        } else {
            Toast.makeText(this, getResources().getString(R.string.gpsActiveNow), Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();


        //initialization of DBHelper
        //again :)
        //it takes context as argument
        dbhelper = new DBHelper(getApplicationContext());

        //getting the intent
        Intent mIntent = getIntent();
        //if user is editting the note we bind the title of this note to editTitle variable
        editTitle = mIntent.getStringExtra("title");
        id = mIntent.getIntExtra("id", 0);

        //we're getting the isEdit value
        //if user is editing note, the value if true
        //otherwise the default value is false
        isEdit = mIntent.getBooleanExtra("isEdit", false);

        //we're checking if user want to edit note
        if (isEdit) {

            // getActionBar().setTitle(R.string.title_activity_update_note);
            //edit note mode
            tgBtn_UpdateLocation.setVisibility(View.VISIBLE);
            btn_confirm.setText(R.string.updateNoteButton);

            Log.d(TAG, "isEdit");
            //getting the readable database
            db = dbhelper.getReadableDatabase();
            Cursor c = dbhelper.getNote(db, id);
            //closing db connection
            db.close();
            //here we're set title and content of note to editText views
            titleEditText.setText(c.getString(0));
            contentEditText.setText(c.getString(1));
            strLocation = c.getString(3);


            String[] latlong = strLocation.split("/");
            lat = Double.parseDouble(latlong[0]);
            lng = Double.parseDouble(latlong[1]);


            //and we're changing the button text to something more appropriate
            //from add note to update note
            //you can change button text in /res/values/strings.xml file

            //addNoteToDB.setText(getResources().getString(R.string.updateNoteButton));
        } else {
            //add note mode
            gpsOpen();
        }

        tgBtn_UpdateLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    gpsOpenUpdate();

                    if (ActivityCompat.checkSelfPermission(AddNote.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddNote.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mMap.setMyLocationEnabled(true);

                    mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                        @Override
                        public void onMyLocationChange(Location arg0) {

                            mMap.clear();

                            AddNote.this.newLocation = arg0;

                            float zoomLevel = (float) 16.0; //This goes up to 21
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()), zoomLevel));
                            mMap.addMarker(new MarkerOptions().position(new LatLng(newLocation.getLatitude(), newLocation.getLongitude())));

                            lat = newLocation.getLatitude();
                            lng = newLocation.getLongitude();

                            strNewLocation = lat + "/" + lng;

                        }
                    });
                } else {

                    mMap.clear();
                    mMap.setMyLocationEnabled(false);
                    float zoomLevel = (float) 16.0; //This goes up to 21
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoomLevel));
                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(editTitle));


                }
            }
        });


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iCancel=new Intent(AddNote.this,MainActivity.class);
                startActivity(iCancel);
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when user clicks button
                //we're grabbing the title and content from editText
                final String title = titleEditText.getText().toString();
                final String content = contentEditText.getText().toString();

                //if user left title or content field empty
                //we show the toast, and tell to user to fill the fields

                if (title.equals("") || content.equals("")) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.validation), Toast.LENGTH_LONG).show();
                    return;
                }

                //adding note to db
                if (!isEdit) {

                    if(strLocation==null){
                        Toast.makeText(AddNote.this,getResources().getString(R.string.locationNotFind),Toast.LENGTH_LONG).show();
                    }
                    else {


                        //if it isn't edit mode we just add a new note to db
                        dbhelper = new DBHelper(getApplicationContext());
                        dbhelper.addNote(title, content, strLocation);
                        //and finish the activity here
                        //so we came back to MainActivity
                        finish();
                    }
                } else {

                    //edit note mode

                    if(tgBtn_UpdateLocation.isChecked()){

                    if(strNewLocation==null){
                        Toast.makeText(AddNote.this,getResources().getString(R.string.locationNotFind),Toast.LENGTH_LONG).show();
                        gpsOpen();
                    }
                    else {

                        //if this is edit mode, we just update the old note
                        dbhelper.updateNote(id, titleEditText.getText().toString(), contentEditText.getText().toString(), strNewLocation);
                        //and the same finish activity
                        finish();
                    }
                    }
                    else {

                        float zoomLevel = (float) 16.0; //This goes up to 21
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoomLevel));
                        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(editTitle)).showInfoWindow();

                        //if this is edit mode, we just update the old note
                        dbhelper.updateNote(id, titleEditText.getText().toString(), contentEditText.getText().toString(), strLocation);
                        //and the same finish activity
                        finish();
                    }
                }
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        dbhelper.close();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;




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


        if(!isEdit){
            //add note mode
            mMap.setMyLocationEnabled(true);

            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location arg0) {


                    mMap.clear();
                    float zoomLevel = (float) 16.0; //This goes up to 21
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(arg0.getLatitude(), arg0.getLongitude()), zoomLevel));
                    mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getLatitude(), arg0.getLongitude())));

                    strLocation = arg0.getLatitude()+"/"+arg0.getLongitude();

                }
            });

        }
        else {
            //edit note mode

            float zoomLevel = (float) 16.0; //This goes up to 21
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoomLevel));
            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(editTitle));



        }




    }
}
