package com.narcoding.localnotepad;

import android.content.Context;
import android.content.ContextWrapper;
import android.location.Location;
import android.media.Image;
import android.speech.tts.Voice;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Naim on 1.11.2016.
 */
public class SaveRead {

    private String filename = "MySampleFile.txt";
    private String filepath = "MyFileStorage";
    File myInternalFile;

    public SaveRead(Context context, Integer noteID, EditText noteBody, Location noteLatLng, Image noteImage, Voice noteVoice) {
        this.context = context;
        this.noteID = noteID;
        this.noteBody = noteBody;
        this.noteLatLng = noteLatLng;
        this.noteImage = noteImage;
        this.noteVoice = noteVoice;
    }

    Context context;
    public Integer noteID;
    public EditText noteBody;
    public Location noteLatLng;
    public Image noteImage;
    public Voice noteVoice;


    //,Location noteLatLng,Image noteImage,Voice noteVoice




    public void saveNote(Context context, Integer noteID, EditText noteBody){

        ContextWrapper contextWrapper = new ContextWrapper(context);
        File directory = contextWrapper.getDir(filepath, Context.MODE_PRIVATE);
        myInternalFile = new File(directory , filename);

        try {
            FileOutputStream fos = new FileOutputStream(myInternalFile);
            fos.write(noteBody.getText().toString().getBytes());
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    String myData = "";

    public String readNote(Integer noteID){

        ContextWrapper contextWrapper = new ContextWrapper(context);
        File directory = contextWrapper.getDir(filepath, Context.MODE_PRIVATE);
        myInternalFile = new File(directory , filename);

        try {
            FileInputStream fis = new FileInputStream(myInternalFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                myData = myData + strLine;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myData;
    }


}
