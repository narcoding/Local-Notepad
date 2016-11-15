package com.narcoding.localnotepad.Activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.narcoding.localnotepad.DBHelper;
import com.narcoding.localnotepad.Item;
import com.narcoding.localnotepad.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    // Tag for debugging
    private static final String TAG = "notepad";

    // our views from layout
    private ListView noteList;
    private SwipeRefreshLayout swipeRefreshLayout;

    // adapter use to populate the listview
    private ArrayAdapter<String> adapter;
    //private ArrayAdapter<String> adapter2;
    // cursor will contain notes from database
    private Cursor notes;
    // database helper
    private DBHelper dbhelper;

    // items contain notes titles
    private ArrayList<String> titles;
    private ArrayList<Item> items;

    // variable will contain the position of clicked item in listview
    private int position = 0;

    //private ArrayList<String> contents;



    private void init(){
        noteList = (ListView) findViewById(R.id.noteList);
        swipeRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        // initialization of database helper
        dbhelper = new DBHelper(getApplicationContext());

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        checkGooglePlayServicesAvailable();
        // setting note's titles to item in listview
        setNotes();

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
        // setting that longclick on listview will open the context menu
        this.registerForContextMenu(noteList);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                refreshContent();

            }
        });


    }




    private void refreshContent(){


        Runnable run= new Runnable() {
            @Override
            public void run() {
                noteList.setAdapter(adapter);
                swipeRefreshLayout.setRefreshing(false);
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
                items.add(new Item(notes.getShort(0), notes.getString(1), notes.getString(2),notes.getString(3)));
            } while (notes.moveToNext());
        }

        for (Item i : items) {
            titles.add(i.getTitle());
        }

        //for(Item i:items){
        //    contents.add(i.getContent());
        //}

        // creating new adapter
        adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item, titles);

        //adapter2=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, contents);

        noteList.setAdapter(adapter);
        // setting listener to the listView

        noteList.setOnItemClickListener(this);

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
        menu.setHeaderTitle(getResources().getString(R.string.CtxMenuHeader));
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

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        return super.onCreateOptionsMenu(menu);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.search:
                //Intent dialer= new Intent(Intent.ACTION_DIAL);
                //startActivity(dialer);
                return true;
            case R.id.action_add:
                //Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                //        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                //startActivityForResult(intent, 1234);
                Intent intent_addNote=new Intent(this,AddNote.class);
                startActivity(intent_addNote);

                return true;

            case R.id.action_map:

                if(adapter.getCount()==0){
                    Toast.makeText(this,"You don't have any note! Please add note!",Toast.LENGTH_LONG).show();
                }
                else {

                    int last_itemId = items.get(noteList.getLastVisiblePosition()).getId();

                    String last_itemTitle = items.get(noteList.getLastVisiblePosition()).getTitle().toString();

                    Intent intent_map = new Intent(this, NotesMap.class);
                    //intent_map.putExtra("last_itemId",last_itemId);
                    //intent_map.putExtra("last_itemTitle",last_itemTitle);
                    startActivity(intent_map);

                return true;
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 0);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        MainActivity.this.finish();
                    }
                });
                dialog.show();
            } else {
                Toast.makeText(this, "This device is not supported.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
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
