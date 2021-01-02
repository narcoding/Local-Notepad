package com.narcoding.localnotepad.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.narcoding.localnotepad.DBHelper
import com.narcoding.localnotepad.R

class OneNote : AppCompatActivity(), OnMapReadyCallback {
    private var mMap_OneNote: GoogleMap? = null

    //our views for display note title and content
    private var noteTitle: TextView? = null
    private var createdAt: TextView? = null
    private var noteContent: TextView? = null
    private var img_showNoteImage: ImageView? = null
    private var img_voiceplay: ImageView? = null

    //dbhelper
    private var dbhelper: DBHelper? = null
    private var db: SQLiteDatabase? = null

    //default values for title and content variables
    private var title = "defaultTitle"
    private var id = 0
    private var content = "defaultContent"
    private var date = "date"
    private var location = "location"
    private var image: ByteArray? = null
    private var voice: ByteArray? = null
    var latitude = 0.0
    var longitude = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_note)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map_OneNote) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        //initialization of DBHelper
        dbhelper = DBHelper(applicationContext)
        noteTitle = findViewById<View>(R.id.noteTitle) as TextView
        noteContent = findViewById<View>(R.id.noteContent) as TextView
        createdAt = findViewById<View>(R.id.createdAt) as TextView
        img_showNoteImage = findViewById<View>(R.id.img_showNoteImage) as ImageView
        img_voiceplay = findViewById<View>(R.id.img_voiceplay) as ImageView
        id = intent.getIntExtra("id", 0)

        //getting the readable database
        db = dbhelper!!.readableDatabase

        //getting the note from database
        val c = dbhelper!!.getNote(db, id)
        //closing the database connection
        db?.close()

        //getting the content from cursor
        //getString(1) because first column is noteTitle and second is noteContent and the third column is date
        title = c!!.getString(0).toString()
        content = c.getString(1).toString()
        date = c.getString(2).toString()
        location = c.getString(3).toString()
        image = c.getBlob(4)
        voice = c.getBlob(5)
        if (location != "null") {
            val latlong = location.split("/").toTypedArray()
            latitude = latlong[0].toDouble()
            longitude = latlong[1].toDouble()
        }


        //setting notes to our viewssss
        noteTitle!!.text = title
        noteContent!!.text = content
        createdAt!!.text = date
        if (image != null) {
            val bitmap = BitmapFactory.decodeByteArray(image, 0, image!!.size)
            img_showNoteImage!!.setImageBitmap(bitmap)
            img_showNoteImage!!.setOnClickListener { //ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                //byte[] byteArray = stream.toByteArray();
                val in1 = Intent(this@OneNote, FullImageActivity::class.java)
                in1.putExtra("image", image)
                startActivity(in1)
            }
        }
        if (voice != null) {
            img_voiceplay!!.setBackgroundResource(android.R.drawable.ic_media_play)
            img_voiceplay!!.setOnClickListener {
                val intentvoice = Intent(this@OneNote, VoiceRecordActivity::class.java)
                intentvoice.putExtra("voice", voice)
                intentvoice.putExtra("fromthere", "OneNote")
                startActivity(intentvoice)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap_OneNote = googleMap
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
        mMap_OneNote!!.isMyLocationEnabled = true

        //LatLngBounds.Builder builder = new LatLngBounds.Builder();
        //for (Marker marker : markers) {
        //    builder.include(marker.getPosition());
        //}
        //LatLngBounds bounds = builder.build();
        //int padding = 0; // offset from edges of the map in pixels
        //CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        if (location != "null") {
            val zoomLevel = 16.0.toFloat() //This goes up to 21
            mMap_OneNote!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoomLevel))
            mMap_OneNote!!.addMarker(MarkerOptions().position(LatLng(latitude, longitude)).title(title)).showInfoWindow()
        }
    }

    companion object {
        private const val TAG = "notepad"
    }
}