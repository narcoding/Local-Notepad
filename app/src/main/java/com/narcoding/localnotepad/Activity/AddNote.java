package com.narcoding.localnotepad.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.drive.internal.StringListResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.narcoding.localnotepad.DBHelper;
import com.narcoding.localnotepad.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AddNote extends ActionBarActivity implements OnMapReadyCallback {

    
    private GoogleMap mMap;
    private Button btn_confirm, btn_cancel,btn_addVoice;
    private ImageButton imgBtn_addImage;
    private ToggleButton tgBtn_UpdateLocation;
    private EditText titleEditText;
    private EditText contentEditText;
    private Location newLocation;

    private static final String TAG = "notepad";

    //this variable will inform us if user want to create a new note or just update.
    private boolean isEdit;

    private DBHelper dbhelper;
    private SQLiteDatabase db;


    //variable will contain the title of editing note
    private String editTitle;
    private String strLocation;
    private String strNewLocation;
    private int id = 0;
    private byte[] image;
    private byte[] voice;

    double lat;
    double lng;

    boolean isGPSEnabled;


    private static final int CAMERA_REQUEST = 1;
    private static final int PICK_FROM_GALLERY = 2;
    private static final int VOICE_REQUEST = 3;

    byte[] imageName;
    int imageId;
    Bitmap theImage;

    private void init() {
        btn_confirm = (Button) findViewById(R.id.btn_confirm);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_addVoice = (Button) findViewById(R.id.btn_addVoice);
        imgBtn_addImage= (ImageButton) findViewById(R.id.imgBtn_addImage);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case CAMERA_REQUEST:

                Bundle extras = data.getExtras();

                if (extras != null) {
                    Bitmap yourImage = extras.getParcelable("data");
                    // convert bitmap to byte
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    yourImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    image = stream.toByteArray();

                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), photo);
                    imgBtn_addImage.setBackgroundDrawable(bitmapDrawable);
                    // Inserting Contacts

                    //Intent i = new Intent(AddNote.this, AddNote.class);
                    //startActivity(i);
                    //finish();

                }
                break;
            case PICK_FROM_GALLERY:
                Bundle extras2 = data.getExtras();

                if (extras2 != null) {
                    Bitmap yourImage = extras2.getParcelable("data");
                    // convert bitmap to byte
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    yourImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    image = stream.toByteArray();

                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), photo);
                    imgBtn_addImage.setBackgroundDrawable(bitmapDrawable);

                    //Intent i = new Intent(AddNote.this, AddNote.class);
                    //startActivity(i);
                    //finish();
                }
                break;
            case VOICE_REQUEST:
                Bundle extras3=data.getExtras();

                if(extras3!=null){

                    if(resultCode == Activity.RESULT_OK){
                         //voice=data.getStringExtra("result");
                        voice=extras3.getByteArray("resultVoice");
                    }

                    //FileInputStream fis = null;
                    //try {
                    //    fis = new FileInputStream(dosyayolu);
                    //} catch (FileNotFoundException e) {
                    //    e.printStackTrace();
                    //}
                    //ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
                    //byte[] buffer =new byte[1024];
                    //int read;
                    //try {
                    //    while ((read = fis.read(buffer)) != -1) {
                    //        baos.write(buffer, 0, read);
                    //    }
                    //} catch (IOException e) {
                    //    e.printStackTrace();
                    //}
                    //try {
                    //    baos.flush();
                    //} catch (IOException e) {
                    //    e.printStackTrace();
                    //}
                    //voice = baos.toByteArray();
                }


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
        //voice=mIntent.getByteArrayExtra("voice");

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
            image=c.getBlob(4);
            voice=c.getBlob(5);


            String[] latlong = strLocation.split("/");
            lat = Double.parseDouble(latlong[0]);
            lng = Double.parseDouble(latlong[1]);

            if(image!=null) {
                ByteArrayInputStream imageStream = new ByteArrayInputStream(image);
                Bitmap theImage = BitmapFactory.decodeStream(imageStream);
                imgBtn_addImage.setImageBitmap(theImage);
            }



        } else {
            //add note mode
            gpsOpen();
        }

        btn_addVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i=new Intent(AddNote.this,VoiceRecordActivity.class);
                i.putExtra("fromthere", "AddNote");
                //startActivityForResult(i,VOICE_REQUEST);
                startActivityForResult(i,VOICE_REQUEST);

                //Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                //startActivityForResult(intent, 2);

            }
        });

        imgBtn_addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final String[] option = new String[] {getResources().getString(R.string.takefromCamera) ,getResources().getString(R.string.selectFromGallery)};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddNote.this,
                        android.R.layout.select_dialog_item, option);
                AlertDialog.Builder builder = new AlertDialog.Builder(AddNote.this);

                builder.setTitle(R.string.CtxMenuHeader);
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Log.e("Selected Item", String.valueOf(which));
                        if (which == 0) {
                            callCamera();
                        }
                        if (which == 1) {
                            callGallery();
                        }

                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();


            }
        });



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
                String title = titleEditText.getText().toString();
                final String content = contentEditText.getText().toString();

                //if user left title or content field empty
                //we show the toast, and tell to user to fill the fields

                if (title.equals("") && content.equals("")) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.validation), Toast.LENGTH_LONG).show();
                    return;
                }
                else if(title.equals("")){

                    int sonkarakter= content.length();
                    if(sonkarakter>8)
                    {
                        title=content.substring(0,8);
                    }
                    else {
                        title=content.substring(0,sonkarakter);
                    }
                }

                //adding note to db
                if (!isEdit) {

                    if(strLocation==null){
                        Toast.makeText(AddNote.this,getResources().getString(R.string.locationNotFind),Toast.LENGTH_LONG).show();
                    }
                    else {


                        //if it isn't edit mode we just add a new note to db
                        dbhelper = new DBHelper(getApplicationContext());
                        dbhelper.addNote(title, content, strLocation,image,voice);
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
                        dbhelper.updateNote(id, title, contentEditText.getText().toString(), strNewLocation,image,voice);
                        //and the same finish activity
                        finish();
                    }
                    }
                    else {

                        float zoomLevel = (float) 16.0; //This goes up to 21
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoomLevel));
                        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(editTitle)).showInfoWindow();

                        //if this is edit mode, we just update the old note
                        dbhelper.updateNote(id, title, contentEditText.getText().toString(), strLocation,image,voice);
                        //and the same finish activity
                        finish();
                    }
                }
            }
        });


    }

    /**
     * open camera method
     */
    public void callCamera() {
        Intent cameraIntent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra("crop", "true");
        cameraIntent.putExtra("aspectX", 0);
        cameraIntent.putExtra("aspectY", 0);
        cameraIntent.putExtra("outputX", 200);
        cameraIntent.putExtra("outputY", 150);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);

        //imgBtn_addImage.setImageBitmap();
    }

    /**
     * open gallery method
     */

    public void callGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(
                Intent.createChooser(intent, "Complete action using"),
                PICK_FROM_GALLERY);

    }


    @Override
    protected void onPause() {
        super.onPause();
        dbhelper.close();
    }

    @Override
    protected void onResume() {
        super.onResume();

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
