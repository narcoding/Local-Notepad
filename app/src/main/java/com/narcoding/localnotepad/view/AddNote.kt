package com.narcoding.localnotepad.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.narcoding.localnotepad.Controller.ImagePicker
import com.narcoding.localnotepad.DBHelper
import com.narcoding.localnotepad.R
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class AddNote : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var btn_confirm: Button? = null
    private var btn_cancel: Button? = null
    private var btn_addVoice: Button? = null
    private var imgBtn_addImage: ImageButton? = null
    private var tgBtn_UpdateLocation: ToggleButton? = null
    private var titleEditText: EditText? = null
    private var contentEditText: EditText? = null
    private var newLocation: Location? = null

    //this variable will inform us if user want to create a new note or just update.
    private var isEdit = false
    private var dbhelper: DBHelper? = null
    private var db: SQLiteDatabase? = null

    //variable will contain the title of editing note
    private var editTitle: String? = null
    private var strLocation: String? = null
    private var strNewLocation: String? = null
    private var id = 0
    private var image: ByteArray? = null
    private var voice: ByteArray? = null
    var lat:Double? = null
    var lng:Double? = null
    var isGPSEnabled = false
    var imagePicker = ImagePicker()
    var imageName: ByteArray? = null
    var imageId = 0
    var theImage: Bitmap? = null
    var iv_attachment: ImageView? = null

    //For Image Attachment
    private val bitmap: Bitmap? = null
    private val file_name: String? = null
    private fun init() {
        btn_confirm = findViewById<View>(R.id.btn_confirm) as Button
        btn_cancel = findViewById<View>(R.id.btn_cancel) as Button
        btn_addVoice = findViewById<View>(R.id.btn_addVoice) as Button
        imgBtn_addImage = findViewById<View>(R.id.imgBtn_addImage) as ImageButton
        tgBtn_UpdateLocation = findViewById<View>(R.id.tgBtnUpdateLocation) as ToggleButton
        tgBtn_UpdateLocation!!.alpha = 0.8f
        tgBtn_UpdateLocation!!.setBackgroundColor(Color.WHITE)
        titleEditText = findViewById<View>(R.id.TitleEditText) as EditText
        contentEditText = findViewById<View>(R.id.ContentEditText) as EditText
        tgBtn_UpdateLocation!!.text = resources.getString(R.string.tgButtonOFF)
        // Sets the text for when the button is first created.
        tgBtn_UpdateLocation!!.textOff = resources.getString(R.string.tgButtonOFF)
        // Sets the text for when the button is not in the checked state.
        tgBtn_UpdateLocation!!.textOn = resources.getString(R.string.tgButtonON)
    }

    private fun gpsOpen() {
        val lm = this.getSystemService(LOCATION_SERVICE) as LocationManager
        isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isGPSEnabled) {
            val builder = AlertDialog.Builder(this@AddNote)
            val action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
            val message = resources.getString(R.string.gpsSeemsDisable)
            builder.setMessage(message)
                    .setPositiveButton(resources.getString(R.string.okey)
                    ) { d, id ->
                        this@AddNote.startActivity(Intent(action))
                        d.dismiss()
                    }
                    .setNegativeButton(resources.getString(R.string.cancel)
                    ) { d, id ->
                        d.cancel()
                        val iBack = Intent(this@AddNote, MainActivity::class.java)
                        startActivity(iBack)
                    }
            builder.create().show()
        } else {
            Toast.makeText(this, resources.getString(R.string.gpsActiveNow), Toast.LENGTH_SHORT).show()
        }
    }

    private fun gpsOpenUpdate() {
        val lm = this.getSystemService(LOCATION_SERVICE) as LocationManager
        isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isGPSEnabled) {
            val builder = AlertDialog.Builder(this@AddNote)
            val action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
            val message = resources.getString(R.string.gpsSeemsDisable)
            builder.setMessage(message)
                    .setPositiveButton(resources.getString(R.string.okey)
                    ) { d, id ->
                        this@AddNote.startActivity(Intent(action))
                        d.dismiss()
                    }
                    .setNegativeButton(resources.getString(R.string.cancel)
                    ) { d, id -> d.cancel() }
            builder.create().show()
        } else {
            Toast.makeText(this, resources.getString(R.string.gpsActiveNow), Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) return
        when (requestCode) {
            CAMERA_REQUEST -> {
                val extras = data!!.extras
                if (extras != null) {
                    val yourImage = extras.getParcelable<Bitmap>("data")
                    // convert bitmap to byte
                    val stream = ByteArrayOutputStream()
                    yourImage!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    image = stream.toByteArray()
                    val photo = data.extras!!["data"] as Bitmap?
                    val bitmapDrawable = BitmapDrawable(resources, photo)
                    imgBtn_addImage!!.setBackgroundDrawable(bitmapDrawable)
                    // Inserting Contacts

                    //Intent i = new Intent(AddNote.this, AddNote.class);
                    //startActivity(i);
                    //finish();
                }
            }
            PICK_FROM_GALLERY -> {
                val extras2 = data!!.extras
                if (extras2 != null) {
                    val yourImage = extras2.getParcelable<Bitmap>("data")
                    // convert bitmap to byte
                    val stream = ByteArrayOutputStream()
                    yourImage!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    image = stream.toByteArray()
                    val photo = data.extras!!["data"] as Bitmap?
                    val bitmapDrawable = BitmapDrawable(resources, photo)
                    imgBtn_addImage!!.setBackgroundDrawable(bitmapDrawable)

                    //Intent i = new Intent(AddNote.this, AddNote.class);
                    //startActivity(i);
                    //finish();
                }
            }
            VOICE_REQUEST -> {
                val extras3 = data!!.extras
                if (extras3 != null) {
                    if (resultCode == RESULT_OK) {
                        //voice=data.getStringExtra("result");
                        voice = extras3.getByteArray("resultVoice")
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


                //Bitmap yourImage = extras4.getParcelable("data");
                val yourImage = imagePicker.getImageFromResult(this@AddNote, resultCode, data)
                // convert bitmap to byte
                val stream = ByteArrayOutputStream()
                yourImage!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
                image = stream.toByteArray()

                //Bitmap photo = (Bitmap) data.getExtras().get("data");
                //BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), photo);
                //imgBtn_addImage.setBackgroundDrawable(bitmapDrawable);
                val bitmapDrawable = BitmapDrawable(resources, imagePicker.getImageFromResult(this@AddNote, resultCode, data))
                imgBtn_addImage!!.setBackgroundDrawable(bitmapDrawable)
            }
            image_REQUEST -> {
                val yourImage = imagePicker.getImageFromResult(this@AddNote, resultCode, data)
                val stream = ByteArrayOutputStream()
                yourImage!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
                image = stream.toByteArray()
                val bitmapDrawable = BitmapDrawable(resources, imagePicker.getImageFromResult(this@AddNote, resultCode, data))
                imgBtn_addImage!!.setBackgroundDrawable(bitmapDrawable)
            }
        }
    }

    /*
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);

        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    getLocation();
                } else {
                    // permission denied, boo! Disable the
                }
                return;
            }
        }
    }

    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        init()


        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        //initialization of DBHelper
        //again :)
        //it takes context as argument
        dbhelper = DBHelper(applicationContext)

        //getting the intent
        val mIntent = intent
        //if user is editting the note we bind the title of this note to editTitle variable
        editTitle = mIntent.getStringExtra("title")
        id = mIntent.getIntExtra("id", 0)
        //voice=mIntent.getByteArrayExtra("voice");

        //we're getting the isEdit value
        //if user is editing note, the value if true
        //otherwise the default value is false
        isEdit = mIntent.getBooleanExtra("isEdit", false)

        //we're checking if user want to edit note
        if (isEdit) {

            // getActionBar().setTitle(R.string.title_activity_update_note);
            //edit note mode
            tgBtn_UpdateLocation!!.visibility = View.VISIBLE
            btn_confirm!!.setText(R.string.updateNoteButton)
            Log.d(TAG, "isEdit")
            //getting the readable database
            db = dbhelper!!.readableDatabase
            val c = dbhelper!!.getNote(db, id)
            //closing db connection
            db?.close()
            //here we're set title and content of note to editText views
            titleEditText!!.setText(c?.getString(0))
            contentEditText!!.setText(c?.getString(1))
            strLocation = c?.getString(3)
            image = c?.getBlob(4)
            voice = c?.getBlob(5)
            val latlong = strLocation?.split("/")?.toTypedArray()
            lat = latlong?.get(0)?.toDouble()
            lng = latlong?.get(1)?.toDouble()
            if (image != null) {
                val imageStream = ByteArrayInputStream(image)
                val theImage = BitmapFactory.decodeStream(imageStream)
                imgBtn_addImage!!.setImageBitmap(theImage)
            }
        } else {
            //add note mode
            gpsOpen()
        }
        btn_addVoice!!.setOnClickListener {
            val i = Intent(this@AddNote, VoiceRecordActivity::class.java)
            i.putExtra("fromthere", "AddNote")
            //startActivityForResult(i,VOICE_REQUEST);
            startActivityForResult(i, VOICE_REQUEST)

            //Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
            //startActivityForResult(intent, 2);
        }
        imgBtn_addImage!!.setOnClickListener {
            startActivityForResult(imagePicker.getPickImageIntent(this@AddNote), image_REQUEST)

            //final String[] option = new String[] {getResources().getString(R.string.takefromCamera) ,getResources().getString(R.string.selectFromGallery)};
            //ArrayAdapter<String> adapter = new ArrayAdapter<>(AddNote.this,
            //        android.R.layout.select_dialog_item, option);
            //AlertDialog.Builder builder = new AlertDialog.Builder(AddNote.this);
//
            //builder.setTitle(R.string.CtxMenuHeader);
            //builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
//
            //    public void onClick(DialogInterface dialog, int which) {
            //        // TODO Auto-generated method stub
            //        Log.e("Selected Item", String.valueOf(which));
            //        if (which == 0) {
            //            callCamera();
            //        }
            //        if (which == 1) {
            //            callGallery();
            //        }
//
            //    }
            //});
            //final AlertDialog dialog = builder.create();
            //dialog.show();
        }
        tgBtn_UpdateLocation!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                gpsOpenUpdate()
                if (ContextCompat.checkSelfPermission(this@AddNote, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this@AddNote, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@OnCheckedChangeListener
                }
                mMap!!.isMyLocationEnabled = true
                mMap!!.setOnMyLocationChangeListener { arg0 ->
                    mMap!!.clear()
                    newLocation = arg0
                    val zoomLevel = 16.0.toFloat() //This goes up to 21
                    mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(newLocation!!.latitude, newLocation!!.longitude), zoomLevel))
                    mMap!!.addMarker(MarkerOptions().position(LatLng(newLocation!!.latitude, newLocation!!.longitude)))
                    lat = newLocation!!.latitude
                    lng = newLocation!!.longitude
                    strNewLocation = "$lat/$lng"
                }
            } else {
                mMap!!.clear()
                mMap!!.isMyLocationEnabled = false
                val zoomLevel = 16.0.toFloat() //This goes up to 21
                mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(lat?.let { lng?.let { it1 -> LatLng(it, it1) } }, zoomLevel))
                mMap!!.addMarker(lat?.let { lng?.let { it1 -> LatLng(it, it1) } }?.let { MarkerOptions().position(it).title(editTitle) })
            }
        })
        btn_cancel!!.setOnClickListener {
            val iCancel = Intent(this@AddNote, MainActivity::class.java)
            startActivity(iCancel)
        }
        btn_confirm!!.setOnClickListener(View.OnClickListener {
            //when user clicks button
            //we're grabbing the title and content from editText
            var title = titleEditText!!.text.toString()
            val content = contentEditText!!.text.toString()

            //if user left title or content field empty
            //we show the toast, and tell to user to fill the fields
            if (title == "" && content == "") {
                Toast.makeText(applicationContext, resources.getString(R.string.validation), Toast.LENGTH_LONG).show()
                return@OnClickListener
            } else if (title == "") {
                val sonkarakter = content.length
                title = if (sonkarakter > 8) {
                    content.substring(0, 8)
                } else {
                    content.substring(0, sonkarakter)
                }
            }

            //adding note to db
            if (!isEdit) {
                if (strLocation == null) {
                    Toast.makeText(this@AddNote, resources.getString(R.string.locationNotFind), Toast.LENGTH_LONG).show()
                } else {


                    //if it isn't edit mode we just add a new note to db
                    dbhelper = DBHelper(applicationContext)
                    dbhelper!!.addNote(title, content, strLocation, image, voice)
                    //and finish the activity here
                    //so we came back to MainActivity
                    finish()
                }
            } else {

                //edit note mode
                if (tgBtn_UpdateLocation!!.isChecked) {
                    if (strNewLocation == null) {
                        Toast.makeText(this@AddNote, resources.getString(R.string.locationNotFind), Toast.LENGTH_LONG).show()
                        gpsOpen()
                    } else {

                        //if this is edit mode, we just update the old note
                        dbhelper!!.updateNote(id, title, contentEditText!!.text.toString(), strNewLocation, image, voice)
                        //and the same finish activity
                        finish()
                    }
                } else {
                    val zoomLevel = 16.0.toFloat() //This goes up to 21
                    mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(lat?.let { it1 -> lng?.let { it2 -> LatLng(it1, it2) } }, zoomLevel))
                    mMap!!.addMarker(lat?.let { it1 -> lng?.let { it2 -> LatLng(it1, it2) } }?.let { it2 -> MarkerOptions().position(it2).title(editTitle) }).showInfoWindow()

                    //if this is edit mode, we just update the old note
                    dbhelper!!.updateNote(id, title, contentEditText!!.text.toString(), strLocation, image, voice)
                    //and the same finish activity
                    finish()
                }
            }
        })
    }

    /**
     * open camera method
     */
    fun callCamera() {
        val cameraIntent = Intent(
                MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra("crop", "true")
        cameraIntent.putExtra("aspectX", 0)
        cameraIntent.putExtra("aspectY", 0)
        cameraIntent.putExtra("outputX", 200)
        cameraIntent.putExtra("outputY", 150)
        startActivityForResult(cameraIntent, CAMERA_REQUEST)

        //imgBtn_addImage.setImageBitmap();
    }

    /**
     * open gallery method
     */
    fun callGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra("crop", "true")
        intent.putExtra("aspectX", 0)
        intent.putExtra("aspectY", 0)
        intent.putExtra("outputX", 200)
        intent.putExtra("outputY", 150)
        intent.putExtra("return-data", true)
        startActivityForResult(
                Intent.createChooser(intent, "Complete action using"),
                PICK_FROM_GALLERY)
    }

    override fun onPause() {
        super.onPause()
        dbhelper!!.close()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        if (!isEdit) {
            //add note mode
            mMap!!.isMyLocationEnabled = true
            mMap!!.setOnMyLocationChangeListener { arg0 ->
                mMap!!.clear()
                val zoomLevel = 16.0.toFloat() //This goes up to 21
                mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(arg0.latitude, arg0.longitude), zoomLevel))
                mMap!!.addMarker(MarkerOptions().position(LatLng(arg0.latitude, arg0.longitude)))
                strLocation = arg0.latitude.toString() + "/" + arg0.longitude
            }
        } else {
            //edit note mode
            val zoomLevel = 16.0.toFloat() //This goes up to 21
            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(lat?.let { lng?.let { it1 -> LatLng(it, it1) } }, zoomLevel))
            mMap!!.addMarker(lat?.let { lng?.let { it1 -> LatLng(it, it1) } }?.let { MarkerOptions().position(it).title(editTitle) })
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {}

    companion object {
        private const val TAG = "notepad"
        private const val CAMERA_REQUEST = 1
        private const val PICK_FROM_GALLERY = 2
        private const val VOICE_REQUEST = 3
        private const val image_REQUEST = 4
    }
}