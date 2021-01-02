package com.narcoding.localnotepad

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Naim on 3.11.2016.
 */
class DBHelper(private val ctx: Context) : SQLiteOpenHelper(ctx, DB_NAME, null, version) {

    //creating the table in database
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    //in case of upgrade we're dropping the old table, and create the new one
    override fun onUpgrade(db: SQLiteDatabase, arg1: Int, arg2: Int) {
        db.execSQL("DROP TABLE IF EXIST " + TABLE_NAME)
        onCreate(db)
    }

    //function for adding the note to database
    fun addNote(title: String?, content: String?, location: String?, image: ByteArray?, voice: ByteArray?) {
        val db = this.writableDatabase
        val dateFormat = SimpleDateFormat("HH:mm' 'dd-MM-yyy")
        val date = Date()
        val datetime = dateFormat.format(date)

        //creating the contentValues object
        //read more here -> http://developer.android.com/reference/android/content/ContentValues.html
        val cv = ContentValues()
        cv.put("noteTitle", title)
        cv.put("noteContent", content)
        cv.put("date", datetime)
        cv.put("location", location)
        cv.put("image", image)
        cv.put("voice", voice)

        //inserting the note to database
        db.insert(TABLE_NAME, null, cv)

        //closing the database connection
        db.close()

        //see that all database connection stuff is inside this method
        //so we don't need to open and close db connection outside this class
    }

    //getting all notes
    fun getNotes(db: SQLiteDatabase): Cursor {
        //db.query is like normal sql query
        //cursor contains all notes
        val c = db.query(TABLE_NAME, arrayOf(KEY_TITLE, KEY_CONTENT), null, null, null, null, "id DESC")
        //moving to the first note
        c.moveToFirst()
        //and returning Cursor object
        return c
    }

    fun getNotes2(db: SQLiteDatabase): Cursor {
        //db.query is like normal sql query
        //cursor contains all notes
        val c = db.query(TABLE_NAME, arrayOf(KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_LOCATION, KEY_IMAGE, KEY_VOICE), null, null, null, null, "id DESC")
        //moving to the first note
        c.moveToFirst()
        //and returning Cursor object
        return c
    }

    fun getNote(db: SQLiteDatabase?, id: Int): Cursor? {
        val c = db?.query(TABLE_NAME, arrayOf(KEY_TITLE, KEY_CONTENT, KEY_DATE, KEY_LOCATION, KEY_IMAGE, KEY_VOICE), KEY_ID + " = ?", arrayOf(id.toString()), null, null, null)
        c?.moveToFirst()
        return c
    }

    fun removeNote(id: Int) {
        val db = writableDatabase
        db.delete(TABLE_NAME, KEY_ID + " = ?", arrayOf(id.toString()))
        db.close()
    }

    fun updateNote(id: Int, title: String?, content: String?, location: String?, image: ByteArray?, voice: ByteArray?) {
        val db = this.writableDatabase
        val dateFormat = SimpleDateFormat("HH:mm' 'dd-MM-yyy")
        val date = Date()
        val datetime = dateFormat.format(date)
        val cv = ContentValues()
        cv.put("noteTitle", title)
        cv.put("noteContent", content)
        cv.put("date", datetime)
        cv.put("location", location)
        cv.put("image", image)
        cv.put("voice", voice)

        //db.update(TABLE_NAME, cv, KEY_TITLE + " LIKE '" +  editTitle +  "'", null);
        db.update(TABLE_NAME, cv, KEY_ID + " = ?", arrayOf(id.toString()))
        db.close()
    }

    companion object {
        //version of database
        private const val version = 1

        //database name
        private const val DB_NAME = "LNDataBase"

        //name of table
        private const val TABLE_NAME = "TBLLocalNotepad"

        //column names
        private const val KEY_ID = "id"
        private const val KEY_TITLE = "noteTitle"
        private const val KEY_CONTENT = "noteContent"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION = "location"
        private const val KEY_IMAGE = "image"
        private const val KEY_VOICE = "voice"

        //sql query to creating table in database
        private const val CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_TITLE + " TEXT NOT NULL, " + KEY_CONTENT + " TEXT NOT NULL, " + KEY_DATE + " TEXT, " + KEY_LOCATION + " LOCATION, " + KEY_IMAGE + " BLOB, " + KEY_VOICE + " BLOB" + ")"
    }
}