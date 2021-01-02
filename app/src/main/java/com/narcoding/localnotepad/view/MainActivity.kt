package com.narcoding.localnotepad.view

import android.Manifest
import android.app.SearchManager
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.*
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import com.baoyz.widget.PullRefreshLayout
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.narcoding.localnotepad.DBHelper
import com.narcoding.localnotepad.model.Item
import com.narcoding.localnotepad.R
import java.util.*

class MainActivity : RuntimePermissionsActivity(), OnItemClickListener {
    // our views from layout
    private var noteList: ListView? = null

    // adapter use to populate the listview
    private var adapter: ArrayAdapter<String>? = null

    // cursor will contain notes from database
    private var notes: Cursor? = null

    // database helper
    private var dbhelper: DBHelper? = null

    // items contain notes titles
    private var titles: ArrayList<String>? = null
    private var items: ArrayList<Item>? = null

    // variable will contain the position of clicked item in listview
    private var position = 0
    private var prl_noteList: PullRefreshLayout? = null
    private var fam: FloatingActionMenu? = null
    private var fabMapsNote: FloatingActionButton? = null
    private var fabAddNote: FloatingActionButton? = null
    private var fabSearchNote: FloatingActionButton? = null
    private var searchView: SearchView? = null
    private var searchItem: MenuItem? = null
    private fun init() {
        noteList = findViewById<View>(R.id.noteList) as ListView
        prl_noteList = findViewById<View>(R.id.prl_noteList) as PullRefreshLayout
        prl_noteList!!.setRefreshStyle(PullRefreshLayout.STYLE_SMARTISAN)
        prl_noteList!!.setOnRefreshListener { refreshContent() }
        // initialization of database helper
        dbhelper = DBHelper(applicationContext)
    }

    private fun initFabMenu() {
        fabMapsNote = findViewById<View>(R.id.fabMapsNote) as FloatingActionButton
        fabAddNote = findViewById<View>(R.id.fabAddNote) as FloatingActionButton
        fabSearchNote = findViewById<View>(R.id.fabSearchNote) as FloatingActionButton
        fam = findViewById<View>(R.id.fab_menu) as FloatingActionMenu

        //handling menu status (open or close)
        fam!!.setOnMenuToggleListener { opened ->
            if (opened) {
                //showToast("Menu is opened");
            } else {
                //showToast("Menu is closed");
            }
        }

        //handling each floating action button clicked
        fabMapsNote!!.setOnClickListener(onButtonClick())
        fabAddNote!!.setOnClickListener(onButtonClick())
        fabSearchNote!!.setOnClickListener(onButtonClick())
        fam!!.setOnClickListener {
            if (fam!!.isOpened) {
                fam!!.close(true)
            }
        }
    }

    private fun onButtonClick(): View.OnClickListener {
        return View.OnClickListener { view ->
            if (view === fabAddNote) {
                this@MainActivity.startActivity(Intent(this@MainActivity, AddNote::class.java))
            } else if (view === fabSearchNote) {
                if (adapter!!.count == 0) {
                    Toast.makeText(this@MainActivity, "You don't have any note! Please add note!", Toast.LENGTH_LONG).show()
                } else {
                    searchItem!!.expandActionView()
                    searchView!!.requestFocus()
                }
            } else {
                if (adapter!!.count == 0) {
                    Toast.makeText(this@MainActivity, "You don't have any note! Please add note!", Toast.LENGTH_LONG).show()
                } else {
                    this@MainActivity.startActivity(Intent(this@MainActivity, NotesMap::class.java))
                }
            }
            fam!!.close(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        initFabMenu()
        setNotes()
        if (Build.VERSION.SDK_INT >= 23) {
            super@MainActivity.requestAppPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAPTURE_AUDIO_OUTPUT,
                    Manifest.permission.CAMERA), R.string.runtimepermission, REQUEST_PERMISSIONS)
        }
        if (adapter!!.count == 0) {
            val builder = AlertDialog.Builder(this@MainActivity)
            val message = resources.getString(R.string.enteranceAlert)
            builder.setTitle(resources.getString(R.string.enteranceAlertTitle))
            builder.setMessage(message)
                    .setPositiveButton(resources.getString(R.string.okey)
                    ) { d, id ->
                        this@MainActivity.startActivity(Intent(this@MainActivity, AddNote::class.java))
                        d.dismiss()
                    }
                    .setNegativeButton(resources.getString(R.string.cancel)
                    ) { d, id -> d.cancel() }
            builder.create().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int) {
        Toast.makeText(this, "Permissions Received.", Toast.LENGTH_LONG).show()
    }

    private fun refreshContent() {
        val run = Runnable {
            noteList!!.adapter = adapter
            prl_noteList!!.setRefreshing(false)
        }
        val handler = Handler()
        handler.postDelayed(run, 750)
    }

    fun setNotes() {
        // init the items arrayList
        titles = ArrayList()
        items = ArrayList()
        //contents=new ArrayList<String>();

        // getting readable database
        val db = dbhelper!!.readableDatabase
        // getting notes from db
        // see dbhelper for more details
        notes = dbhelper!!.getNotes2(db)

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

        // creating new adapter
        adapter = ArrayAdapter(this, R.layout.simple_list_item, titles!!)
        noteList!!.adapter = adapter
        // setting listener to the listView
        noteList!!.onItemClickListener = this
        registerForContextMenu(noteList)
        stopManagingCursor(notes)
    }

    // always when we start this activity we want to refresh the list of notes
    override fun onResume() {
        super.onResume()
        adapter!!.notifyDataSetChanged()
        setNotes()
    }

    // this method is called when user long clicked on listview
    override fun onCreateContextMenu(menu: ContextMenu, v: View,
                                     menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        // using menuInfo to determine which item of listview was clicked
        val info = menuInfo as AdapterContextMenuInfo
        position = info.position
        // setting the title of context menu header
        val tv = noteList!!.getChildAt(position) as TextView
        menu.setHeaderTitle(tv.text)
        // inflating the menu from xml file
        // for details see context_menu.xml file in /res/menu folder
        val inflater = menuInflater
        inflater.inflate(R.menu.context_menu, menu)
    }

    // method is called when user clicks on contextmenu item
    override fun onContextItemSelected(item: MenuItem): Boolean {
        // getting the textView from ListView which item was clicked
        val tv = noteList!!.getChildAt(position) as TextView
        // getting the title of this textView
        val title = tv.text.toString()
        when (item.itemId) {
            R.id.showNote -> {
                val mIntent = Intent(this, OneNote::class.java)
                mIntent.putExtra("id", items!![position].id)
                startActivity(mIntent)
            }
            R.id.editNote -> {
                val i = Intent(this, AddNote::class.java)
                i.putExtra("id", items!![position].id)
                Log.d(TAG, title)
                // this is important
                // we send boolean to CreateNote activity
                // thanks to this boolean activity knows that user want to edit
                // notes
                i.putExtra("isEdit", true)
                startActivity(i)
            }
            R.id.removeNote -> {
                // removing this notes
                dbhelper!!.removeNote(items!![position].id)
                // refreshing the listView
                setNotes()
            }
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        searchItem = menu.findItem(R.id.action_search)
        val manager = getSystemService(SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView!!.setSearchableInfo(manager.getSearchableInfo(componentName))
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter!!.filter.filter(newText)
                return false
            }
        })
        return true
    }

    override fun onItemClick(arg0: AdapterView<*>?, arg1: View, arg2: Int, arg3: Long) {
        val tv = arg1 as TextView
        val title = tv.text.toString()
        val mIntent = Intent(this, OneNote::class.java)
        mIntent.putExtra("title", title)
        mIntent.putExtra("id", items!![arg2].id)
        startActivity(mIntent)
    }

    companion object {
        private const val REQUEST_PERMISSIONS = 20

        // Tag for debugging
        private const val TAG = "notepad"
    }
}