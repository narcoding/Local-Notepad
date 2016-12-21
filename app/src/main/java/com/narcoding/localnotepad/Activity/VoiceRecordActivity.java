package com.narcoding.localnotepad.Activity;

import android.graphics.PorterDuff;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import com.narcoding.localnotepad.R;

import java.io.IOException;

public class VoiceRecordActivity extends AppCompatActivity {

    private ImageButton imgbtn_record;
    private ImageButton audio_startplay_Button;
    private ImageButton audio_stopplay_Button;
    private Chronometer chronometer;
    private MediaRecorder mediaRecorder=null;
    private MediaPlayer mediaPlayer=null;
    private String FILE_PATH_NAME=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_record);

        imgbtn_record= (ImageButton) findViewById(R.id.imgbtn_record);
        audio_startplay_Button= (ImageButton) findViewById(R.id.audio_startplay_Button);
        audio_stopplay_Button= (ImageButton) findViewById(R.id.audio_stopplay_Button);

        chronometer=(Chronometer) findViewById(R.id.chronometer);
        mediaRecorder = new MediaRecorder();
        mediaPlayer = new MediaPlayer();

        chronometer.setFormat("%s");
        FILE_PATH_NAME = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        FILE_PATH_NAME += "/recordsample.3gp";

        audio_stopplay_Button.setVisibility(View.GONE);
        audio_startplay_Button.setVisibility(View.GONE);

        imgbtn_record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_UP:{
                        ImageButton view= (ImageButton) v;
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        mediaRecorder.stop();
                        mediaRecorder.reset();
                        mediaRecorder.release();
                        mediaRecorder=null;
                        chronometer.stop();
                        audio_startplay_Button.setVisibility(View.VISIBLE);

                        break;
                    }
                    case MotionEvent.ACTION_DOWN:{
                        ImageButton view= (ImageButton) v;
                        view.getBackground().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();

                        chronometer.start();

                        mediaRecorder = new MediaRecorder();
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        mediaRecorder.setOutputFile(FILE_PATH_NAME);
                        try {
                            mediaRecorder.prepare();
                        } catch (IllegalStateException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        mediaRecorder.start();

                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:{
                        ImageButton view= (ImageButton) v;
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        break;
                    }

                }

                return true;
            }
        });

        audio_startplay_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mediaPlayer.setDataSource(FILE_PATH_NAME);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
                audio_startplay_Button.setVisibility(View.GONE);
                audio_stopplay_Button.setVisibility(View.VISIBLE);
            }

        });

        audio_stopplay_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                audio_startplay_Button.setVisibility(View.VISIBLE);
                audio_stopplay_Button.setVisibility(View.GONE);
            }
        });

    }
}
