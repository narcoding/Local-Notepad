package com.narcoding.localnotepad.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.narcoding.localnotepad.DBHelper
import com.narcoding.localnotepad.model.Item
import com.narcoding.localnotepad.R
import java.util.*

class NotesMap : AppCompatActivity(), OnMapReadyCallback, OnInfoWindowClickListener {
    private var mMap_Notes: GoogleMap? = null
    private var notes: Cursor? = null
    var dbHelper: DBHelper? = null

    // items contain notes titles
    private var titles: ArrayList<String>? = null
    private var items: ArrayList<Item>? = null
    private var locations: ArrayList<String>? = null
    private var markers: ArrayList<Marker>? = null
    private var latitude: ArrayList<Double>? = null
    private var longitude: ArrayList<Double>? = null

    // variable will contain the position of clicked item in listview
    private val position = 0
    var c = 0
    var isGPSEnabled = false
    var cu: CameraUpdate? = null
    var bounds: LatLngBounds? = null
    private var imgBtn_allmarkers: ImageButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes_map)
        dbHelper = DBHelper(applicationContext)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.Map_Notes) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        setNotes()
        imgBtn_allmarkers = findViewById<View>(R.id.imgBtn_allmarkers) as ImageButton
        imgBtn_allmarkers!!.alpha = 0.8f
        imgBtn_allmarkers!!.setBackgroundColor(Color.WHITE)
    }

    private fun gpsOpen() {
        val lm = this.getSystemService(LOCATION_SERVICE) as LocationManager
        isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isGPSEnabled) {
            val builder = AlertDialog.Builder(this@NotesMap)
            val action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
            val message = resources.getString(R.string.gpsSeemsDisable)
            builder.setMessage(message)
                    .setPositiveButton(resources.getString(R.string.okey)
                    ) { d, id ->
                        this@NotesMap.startActivity(Intent(action))
                        d.dismiss()
                    }
                    .setNegativeButton(resources.getString(R.string.cancel)
                    ) { d, id ->
                        d.cancel()
                        Toast.makeText(this@NotesMap, resources.getString(R.string.gpsDidntOpen), Toast.LENGTH_SHORT).show()
                    }
            builder.create().show()
        } else {
            Toast.makeText(this, resources.getString(R.string.gpsActiveNow), Toast.LENGTH_SHORT).show()
        }
    }

    fun setNotes() {
        // init the items arrayList
        titles = ArrayList()
        items = ArrayList()
        locations = ArrayList()
        latitude = ArrayList()
        longitude = ArrayList()
        markers = ArrayList()

        // getting readable database
        val db = dbHelper!!.readableDatabase
        // getting notes from db
        // see dbhelper for more details
        notes = dbHelper!!.getNotes2(db)


        // this should fix the problem
        // now the activity will be managing the cursor lifecycle
        startManagingCursor(notes)

        // closing database connection !important
        // always close connection with database
        // we closing database connection here because we don't use db anymore
        db.close()

        // populating ArrayList items with notes titles
        if (notes!!.moveToFirst()) {
            do {
                items!!.add(Item(notes!!.getShort(0).toInt(), notes!!.getString(1), notes!!.getString(2), notes!!.getString(3), notes!!.getBlob(4), notes!!.getBlob(5)))
            } while (notes!!.moveToNext())
        }
        for (i in items!!) {
            titles!!.add(i.title)
        }
        locations!!.removeAll(setOf<Any?>(null))
        for (i in items!!) {
            locations!!.add(i.location)
        }
        for (a in locations!!.indices) {
            val latlong = locations!![a].split("/").toTypedArray()
            latitude!!.add(latlong[0].toDouble())
            longitude!!.add(latlong[1].toDouble())
        }
        stopManagingCursor(notes)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap_Notes = googleMap
        googleMap.setOnInfoWindowClickListener(this)
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
        mMap_Notes!!.isMyLocationEnabled = true
        setNotes()
        for (a in locations!!.indices) {
            markers!!.add(mMap_Notes!!.addMarker(MarkerOptions().position(LatLng(latitude!![a], longitude!![a]))))
        }
        var b = 0
        for (i in items!!) {
            if (b < markers!!.size) {
                markers!![b].title = i.title
                b = b + 1
            }
        }
        val builder = LatLngBounds.Builder()
        for (marker in markers!!) {
            builder.include(marker.position)
        }
        bounds = builder.build()
        val padding = 80 // offset from edges of the map in pixels
        cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        mMap_Notes!!.setOnMapLoadedCallback { //Your code where exception occurs goes here...
            mMap_Notes!!.animateCamera(cu)
        }
        gpsOpen()


        //mMap_Notes.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

        //    @Override

        //    public void onMyLocationChange(Location location) {

        //

        //        float zoomLevel = (float) 16.0; //This goes up to 21

        //        mMap_Notes.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoomLevel));

        //

        //    }

        //});
        imgBtn_allmarkers!!.setOnClickListener {
            mMap_Notes!!.setOnMapLoadedCallback { //Your code where exception occurs goes here...
                mMap_Notes!!.animateCamera(cu)
            }
        }
    }

    override fun onInfoWindowClick(marker: Marker) {
        //String title = marker.getTitle().toString();
        //Intent mIntent = new Intent(this, OneNote.class);
        //mIntent.putExtra("id", marker.getId());
        //startActivity(mIntent);
    }
}