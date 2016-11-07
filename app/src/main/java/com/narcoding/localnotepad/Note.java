package com.narcoding.localnotepad;

/**
 * Created by Naim on 31.10.2016.
 */
public class Note {
    public int noteId;
    public String noteBody;
    public double latitude;
    public double longitude;

    public Note(int noteId, String noteBody, double latitude, double longitude) {
        this.noteId = noteId;
        this.noteBody = noteBody;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
