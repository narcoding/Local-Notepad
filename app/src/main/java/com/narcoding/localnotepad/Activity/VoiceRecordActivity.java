package com.narcoding.localnotepad.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.narcoding.localnotepad.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class VoiceRecordActivity extends AppCompatActivity {

    private ImageButton imgbtn_record;
    private Chronometer chronometer;
    private MediaRecorder mediaRecorder=null;
    private MediaPlayer mediaPlayer=null;
    private String FILE_PATH_NAME=null;
    private SeekBar seekBar;
    private Handler handler;
    private Boolean isRecording;
    private int recordTime,playTime;
    private String fromthere;
    private byte[] voice;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_record);



        imgbtn_record= (ImageButton) findViewById(R.id.imgbtn_record);
        chronometer=(Chronometer) findViewById(R.id.chronometer);
        seekBar=(SeekBar)findViewById(R.id.seek1);

        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });


        mediaRecorder = new MediaRecorder();
        mediaPlayer = new MediaPlayer();
        handler=new Handler();

        isRecording=false;

        chronometer.setFormat("%s");
        FILE_PATH_NAME = Environment.getExternalStorageDirectory().getAbsolutePath();
        FILE_PATH_NAME += "/notepadrecord.3gp";

        seekBar.setVisibility(View.GONE);


        fromthere = getIntent().getStringExtra("fromthere");

        if(fromthere.equals("AddNote")){

            imgbtn_record.setBackgroundResource(R.drawable.voicerecordimagebutton);
            seekBar.setVisibility(View.GONE);

            imgbtn_record.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()){
                        case MotionEvent.ACTION_UP:{
                            ImageButton view= (ImageButton) v;
                            view.setBackgroundResource(R.drawable.voicerecordimagebutton);
                            stopRecording();

                            final AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRecordActivity.this);
                            final String message = getResources().getString(R.string.saveVoice);

                            builder.setMessage(message)
                                    .setPositiveButton(getResources().getString(R.string.saveThis),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface d, int id) {
                                                    try {
                                                        voice=convert(FILE_PATH_NAME);

                                                        File gecicisesdosyası = new File(FILE_PATH_NAME);
                                                        gecicisesdosyası.delete();

                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                    Intent returnIntent = new Intent();
                                                    returnIntent.putExtra("resultVoice",voice);
                                                    setResult(Activity.RESULT_OK,returnIntent);

                                                    finish();
                                                    d.dismiss();
                                                }
                                            })
                                    .setNegativeButton(getResources().getString(R.string.deleteThis),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface d, int id) {

                                                    seekBar.setVisibility(View.GONE);

                                                    d.cancel();
                                                }
                                            });
                            builder.create().show();

                            isRecording=false;

                            break;
                        }
                        case MotionEvent.ACTION_DOWN:{
                            ImageButton view= (ImageButton) v;
                            view.setBackgroundResource(R.drawable.voicerecordimagebutton1);

                            startRecording();
                            break;
                        }
                        case MotionEvent.ACTION_CANCEL:{
                            ImageButton view= (ImageButton) v;
                            view.setBackgroundResource(R.drawable.voicerecordimagebutton1);
                            break;
                        }

                    }

                    return true;
                }
            });
        }
        else if(fromthere.equals("OneNote")){
            imgbtn_record.setBackgroundResource(R.drawable.playbutton);
            chronometer.setVisibility(View.GONE);
            seekBar.setVisibility(View.VISIBLE);

            voice = getIntent().getByteArrayExtra("voice");
            imgbtn_record.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    play3GP(voice);
                }
            });


        }

    }


    public void startRecording(){
        if(!isRecording){
            //Create MediaRecorder and initialize audio source, output format, and audio encoder
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource( MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat( MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(FILE_PATH_NAME);
            mediaRecorder.setAudioEncoder( MediaRecorder.AudioEncoder.AMR_NB);
            // Starting record time
            recordTime=0;

            chronometer.start();
            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                Log.e("LOG_TAG", "prepare failed");
            }
            // Start record job
            mediaRecorder.start();
            // Change isRecroding flag to true
            isRecording=true;
            // Post the record progress
            handler.post(UpdateRecordTime);
        }
    }
    public void stopRecording(){
        if(isRecording){
            // Stop recording and release resource
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            // Change isRecording flag to false
            isRecording=false;
            // Hide TextView that shows record time
            //tv.setVisibility(TextView.GONE);
            chronometer.stop();

        }
    }

    public void playIt(){

        // Reset max and progress of the SeekBar
        seekBar.setMax(recordTime);
        seekBar.setProgress(0);
        try {
            // Initialize the player and start playing the audio
            mediaPlayer.setDataSource(FILE_PATH_NAME);
            mediaPlayer.prepare();
            mediaPlayer.start();
            // Post the play progress
            handler.post(UpdatePlayTime);
        } catch (IOException e) {
            Log.e("LOG_TAG", "prepare failed");
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                imgbtn_record.setBackgroundResource(R.drawable.playbutton);
            }
        });
    }

    private void play3GP(final byte[] voice) {

        imgbtn_record.setBackgroundResource(R.drawable.pausebutton);
        imgbtn_record.setClickable(false);

        try {
            File temp3GP = File.createTempFile("temp_audio", "3gp", getCacheDir());
            temp3GP.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(temp3GP);
            fos.write(voice);
            fos.close();

            playTime=0;
            seekBar.setProgress(0);
            //mediaPlayer.seekTo(seekBar.getId());
            //seekBar.setMax(mediaPlayer.getDuration());


            FileInputStream fis = new FileInputStream(temp3GP);
            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();
            mediaPlayer.start();
            handler.post(UpdatePlayTime);

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    imgbtn_record.setBackgroundResource(R.drawable.playbutton);
                    finish();

                }
            });

        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }


    Runnable UpdateRecordTime=new Runnable(){
        public void run(){
            if(isRecording){

                recordTime+=1;
                // Delay 1s before next call
                handler.postDelayed(this, 1000);
                chronometer.start();

            }
        }
    };
    Runnable UpdatePlayTime=new Runnable(){
        public void run(){
            if(mediaPlayer.isPlaying()){

                // Update play time and SeekBar
                playTime+=1;
                //seekBar.setProgress(playTime);
                seekBar.setMax(mediaPlayer.getDuration());
                seekBar.setProgress(mediaPlayer.getCurrentPosition());

                // Delay 1s before next call
                handler.postDelayed(this, 1000);

            }
        }
    };


    public byte[] convert(String path) throws IOException {

        FileInputStream fis = new FileInputStream(path);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];

        for (int readNum; (readNum = fis.read(b)) != -1;) {
            bos.write(b, 0, readNum);
        }

        byte[] bytes = bos.toByteArray();

        return bytes;
    }

}
