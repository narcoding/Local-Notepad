package com.narcoding.localnotepad;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class AddNote extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btn_confirm,btn_cancel;
    private EditText titleEditText;
    private EditText contentEditText;

    Location latLng;

    private static final String TAG = "notepad";


    //this variable will inform us if user want to create a new note or just update
    private boolean isEdit;


    private DBHelper dbhelper;
    private SQLiteDatabase db;

    //variable will contain the title of editing note
    private String editTitle;
    private int id = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        btn_confirm= (Button) findViewById(R.id.btn_confirm);
        btn_cancel= (Button) findViewById(R.id.btn_cancel);
        titleEditText = (EditText) findViewById(R.id.TitleEditText);
        contentEditText = (EditText) findViewById(R.id.ContentEditText);

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
        if(isEdit) {
            Log.d(TAG, "isEdit");
            //getting the readable database
            db = dbhelper.getReadableDatabase();
            Cursor c = dbhelper.getNote(db, id);
            //closing db connection
            db.close();
            //here we're set title and content of note to editText views
            titleEditText.setText(c.getString(0));
            contentEditText.setText(c.getString(1));
            //and we're changing the button text to something more appropriate
            //from add note to update note
            //you can change button text in /res/values/strings.xml file

            //addNoteToDB.setText(getResources().getString(R.string.updateNoteButton));
        }




        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when user clicks button
                //we're grabbing the title and content from editText
                String title = titleEditText.getText().toString();
                String content = contentEditText.getText().toString();

                //if user left title or content field empty
                //we show the toast, and tell to user to fill the fields

                if (title.equals("") || content.equals("")) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.validation), Toast.LENGTH_LONG).show();
                    return;
                }

                //adding note to db
                if (!isEdit) {
                    //if it isn't edit mode we just add a new note to db
                    dbhelper = new DBHelper(getApplicationContext());
                    dbhelper.addNote(title, content,latLng);
                    //and finish the activity here
                    //so we came back to Simple_NotepadActivity
                    finish();
                } else {
                    //if this is edit mode, we just update the old note
                    dbhelper.updateNote(title, content, editTitle,latLng);
                    //and the same finish activity
                    finish();
                }


            }
        });

    }


    @Override
    protected void onPause() {
        super.onPause();
        dbhelper.close();
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
        mMap.setMyLocationEnabled(true);

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location arg0) {

                mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getLatitude(), arg0.getLongitude())).title("It's Me!"));

                latLng=new Location(arg0);
                //if (arg0!=null){
                //    latLng.setLatitude(arg0.getLatitude());
                //    latLng.setLongitude(arg0.getLongitude());
                //}


            }
        });
        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(41, 29);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in İstanbul"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

}
