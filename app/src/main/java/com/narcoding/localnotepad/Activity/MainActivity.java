package com.narcoding.localnotepad.Activity;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.narcoding.localnotepad.DBHelper;
import com.narcoding.localnotepad.Item;
import com.narcoding.localnotepad.R;

import java.util.ArrayList;
public class MainActivity extends RuntimePermissionsActivity implements AdapterView.OnItemClickListener{

    private static final int REQUEST_PERMISSIONS = 20;

    // Tag for debugging
    private static final String TAG = "notepad";

    // our views from layout
    private ListView noteList;

    // adapter use to populate the listview
    private ArrayAdapter<String> adapter;

    // cursor will contain notes from database
    private Cursor notes;
    // database helper
    private DBHelper dbhelper;

    // items contain notes titles
    private ArrayList<String> titles;
    private ArrayList<Item> items;

    // variable will contain the position of clicked item in listview
    private int position = 0;

    private PullRefreshLayout prl_noteList;
    private FloatingActionMenu fam;
    private FloatingActionButton fabMapsNote, fabAddNote, fabSearchNote;
    private SearchView searchView;
    private MenuItem searchItem;


    private void init() {
        noteList = (ListView) findViewById(R.id.noteList);

        prl_noteList= (PullRefreshLayout) findViewById(R.id.prl_noteList);
        prl_noteList.setRefreshStyle(PullRefreshLayout.STYLE_SMARTISAN);
        prl_noteList.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });
        // initialization of database helper
        dbhelper = new DBHelper(getApplicationContext());

    }

    private void initFabMenu(){
        fabMapsNote = (FloatingActionButton) findViewById(R.id.fabMapsNote);
        fabAddNote = (FloatingActionButton) findViewById(R.id.fabAddNote);
        fabSearchNote = (FloatingActionButton) findViewById(R.id.fabSearchNote);
        fam = (FloatingActionMenu) findViewById(R.id.fab_menu);

        //handling menu status (open or close)
        fam.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if (opened) {
                    //showToast("Menu is opened");
                } else {
                    //showToast("Menu is closed");
                }
            }
        });

        //handling each floating action button clicked
        fabMapsNote.setOnClickListener(onButtonClick());
        fabAddNote.setOnClickListener(onButtonClick());
        fabSearchNote.setOnClickListener(onButtonClick());

        fam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fam.isOpened()) {
                    fam.close(true);
                }
            }
        });
    }

    private View.OnClickListener onButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == fabAddNote) {

                    MainActivity.this.startActivity(new Intent(MainActivity.this,AddNote.class));

                } else if (view == fabSearchNote) {

                    if(adapter.getCount()==0){
                        Toast.makeText(MainActivity.this,"You don't have any note! Please add note!",Toast.LENGTH_LONG).show();
                    }
                    else {
                        searchItem.expandActionView();
                        searchView.requestFocus();
                    }


                } else {
                    if(adapter.getCount()==0){
                        Toast.makeText(MainActivity.this,"You don't have any note! Please add note!",Toast.LENGTH_LONG).show();
                    }
                    else {
                        MainActivity.this.startActivity(new Intent(MainActivity.this,NotesMap.class));
                    }

                }
                fam.close(true);
            }
        };
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initFabMenu();
        setNotes();

        if (Build.VERSION.SDK_INT >= 23) {
            MainActivity.super.requestAppPermissions(new
                            String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.CAPTURE_AUDIO_OUTPUT,
                            Manifest.permission.CAMERA}, R.string
                            .runtimepermission
                    , REQUEST_PERMISSIONS);
        }

        if(adapter.getCount()==0){

            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            final String message = getResources().getString(R.string.enteranceAlert);

            builder.setTitle(getResources().getString(R.string.enteranceAlertTitle));
            builder.setMessage(message)
                    .setPositiveButton(getResources().getString(R.string.okey),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    MainActivity.this.startActivity(new Intent(MainActivity.this,AddNote.class));
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


        }
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        Toast.makeText(this, "Permissions Received.", Toast.LENGTH_LONG).show();
    }


    private void refreshContent(){

        Runnable run= new Runnable() {
            @Override
            public void run() {
                noteList.setAdapter(adapter);
                prl_noteList.setRefreshing(false);
            }
        };

        android.os.Handler handler=new android.os.Handler();
        handler.postDelayed(run, 750);

    }

    public void setNotes() {
        // init the items arrayList
        titles = new ArrayList<String>();
        items = new ArrayList<Item>();
        //contents=new ArrayList<String>();

        // getting readable database
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        // getting notes from db
        // see dbhelper for more details
        notes = dbhelper.getNotes2(db);

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
                items.add(new Item(notes.getShort(0), notes.getString(1), notes.getString(2),notes.getString(3),notes.getBlob(4),notes.getBlob(5)));
            } while (notes.moveToNext());
        }

        for (Item i : items) {
            titles.add(i.getTitle());
        }

        // creating new adapter
        adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item, titles);

        noteList.setAdapter(adapter);
        // setting listener to the listView

        noteList.setOnItemClickListener(this);
        registerForContextMenu(noteList);

        stopManagingCursor(notes);

    }

    // always when we start this activity we want to refresh the list of notes
    @Override
    protected void onResume() {
        super.onResume();

        adapter.notifyDataSetChanged();
        setNotes();

    }

    // this method is called when user long clicked on listview
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // using menuInfo to determine which item of listview was clicked
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        position = info.position;
        // setting the title of context menu header
        TextView tv = (TextView) noteList.getChildAt(position);
        menu.setHeaderTitle(tv.getText());
        // inflating the menu from xml file
        // for details see context_menu.xml file in /res/menu folder
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);

    }

    // method is called when user clicks on contextmenu item
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // getting the textView from ListView which item was clicked
        TextView tv = (TextView) noteList.getChildAt(position);
        // getting the title of this textView
        String title = tv.getText().toString();

        // performing one of actions, depending on user choice
        switch (item.getItemId()) {

            case R.id.showNote:
                Intent mIntent = new Intent(this, OneNote.class);
                mIntent.putExtra("id", items.get(position).getId());
                startActivity(mIntent);
                break;

            case R.id.editNote:
                Intent i = new Intent(this, AddNote.class);
                i.putExtra("id", items.get(position).getId());
                Log.d(TAG, title);
                // this is important
                // we send boolean to CreateNote activity
                // thanks to this boolean activity knows that user want to edit
                // notes
                i.putExtra("isEdit", true);
                startActivity(i);
                break;

            case R.id.removeNote:
                // removing this notes
                dbhelper.removeNote(items.get(position).getId());
                // refreshing the listView
                setNotes();
                break;
        }

        return false;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        searchItem =  menu.findItem(R.id.action_search);

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }

        });
        return true;

    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

        TextView tv = (TextView) arg1;
        String title = tv.getText().toString();
        Intent mIntent = new Intent(this, OneNote.class);
        mIntent.putExtra("title", title);
        mIntent.putExtra("id", items.get(arg2).getId());
        startActivity(mIntent);

    }

}
