package com.narcoding.localnotepad.view

import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Chronometer
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.narcoding.localnotepad.R
import java.io.*

class VoiceRecordActivity : AppCompatActivity() {
    private var imgbtn_record: ImageButton? = null
    private var chronometer: Chronometer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var FILE_PATH_NAME: String? = null
    private var seekBar: SeekBar? = null
    private var handler: Handler? = null
    private var isRecording: Boolean? = null
    private var recordTime = 0
    private var playTime = 0
    private var fromthere: String? = null
    private var voice: ByteArray? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_record)
        imgbtn_record = findViewById<View>(R.id.imgbtn_record) as ImageButton
        chronometer = findViewById<View>(R.id.chronometer) as Chronometer
        seekBar = findViewById<View>(R.id.seek1) as SeekBar
        seekBar!!.setOnTouchListener { view, motionEvent -> true }
        mediaRecorder = MediaRecorder()
        mediaPlayer = MediaPlayer()
        handler = Handler()
        isRecording = false
        chronometer!!.format = "%s"
        FILE_PATH_NAME = Environment.getExternalStorageDirectory().absolutePath
        FILE_PATH_NAME += "/notepadrecord.3gp"
        seekBar!!.visibility = View.GONE
        fromthere = intent.getStringExtra("fromthere")
        if (fromthere == "AddNote") {
            imgbtn_record!!.setBackgroundResource(R.drawable.voicerecordimagebutton)
            seekBar!!.visibility = View.GONE
            imgbtn_record!!.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        val view = v as ImageButton
                        view.setBackgroundResource(R.drawable.voicerecordimagebutton)
                        stopRecording()
                        val builder = AlertDialog.Builder(this@VoiceRecordActivity)
                        val message = resources.getString(R.string.saveVoice)
                        builder.setMessage(message)
                                .setPositiveButton(resources.getString(R.string.saveThis)
                                ) { d, id ->
                                    try {
                                        voice = convert(FILE_PATH_NAME)
                                        val gecicisesdosyası = File(FILE_PATH_NAME)
                                        gecicisesdosyası.delete()
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                    val returnIntent = Intent()
                                    returnIntent.putExtra("resultVoice", voice)
                                    setResult(RESULT_OK, returnIntent)
                                    finish()
                                    d.dismiss()
                                }
                                .setNegativeButton(resources.getString(R.string.deleteThis)
                                ) { d, id ->
                                    seekBar!!.visibility = View.GONE
                                    d.cancel()
                                }
                        builder.create().show()
                        isRecording = false
                    }
                    MotionEvent.ACTION_DOWN -> {
                        val view = v as ImageButton
                        view.setBackgroundResource(R.drawable.voicerecordimagebutton1)
                        startRecording()
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        val view = v as ImageButton
                        view.setBackgroundResource(R.drawable.voicerecordimagebutton1)
                    }
                }
                true
            }
        } else if (fromthere == "OneNote") {
            imgbtn_record!!.setBackgroundResource(R.drawable.playbutton)
            chronometer!!.visibility = View.GONE
            seekBar!!.visibility = View.VISIBLE
            voice = intent.getByteArrayExtra("voice")
            imgbtn_record!!.setOnClickListener { play3GP(voice) }
        }
    }

    fun startRecording() {
        if (!isRecording!!) {
            //Create MediaRecorder and initialize audio source, output format, and audio encoder
            mediaRecorder = MediaRecorder()
            //mediaRecorder.setAudioSource( MediaRecorder.AudioSource.MIC);
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder!!.setOutputFile(FILE_PATH_NAME)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            // Starting record time
            recordTime = 0
            chronometer!!.start()
            try {
                mediaRecorder!!.prepare()
                // Start record job
                mediaRecorder!!.start()
            } catch (e: IOException) {
                Log.e("LOG_TAG", "prepare failed")
                Toast.makeText(this, "Telefonunuz ses kaydı özelliğini desteklememektedir.", Toast.LENGTH_SHORT)
            }

            // Change isRecroding flag to true
            isRecording = true
            // Post the record progress
            handler!!.post(UpdateRecordTime)
        }
    }

    fun stopRecording() {
        if (isRecording!!) {
            // Stop recording and release resource
            mediaRecorder!!.stop()
            mediaRecorder!!.release()
            mediaRecorder = null
            // Change isRecording flag to false
            isRecording = false
            // Hide TextView that shows record time
            //tv.setVisibility(TextView.GONE);
            chronometer!!.stop()
        }
    }

    fun playIt() {

        // Reset max and progress of the SeekBar
        seekBar!!.max = recordTime
        seekBar!!.progress = 0
        try {
            // Initialize the player and start playing the audio
            mediaPlayer!!.setDataSource(FILE_PATH_NAME)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
            // Post the play progress
            handler!!.post(UpdatePlayTime)
        } catch (e: IOException) {
            Log.e("LOG_TAG", "prepare failed")
        }
        mediaPlayer!!.setOnCompletionListener { imgbtn_record!!.setBackgroundResource(R.drawable.playbutton) }
    }

    private fun play3GP(voice: ByteArray?) {
        imgbtn_record!!.setBackgroundResource(R.drawable.pausebutton)
        imgbtn_record!!.isClickable = false
        try {
            val temp3GP = File.createTempFile("temp_audio", "3gp", cacheDir)
            temp3GP.deleteOnExit()
            val fos = FileOutputStream(temp3GP)
            fos.write(voice)
            fos.close()
            playTime = 0
            seekBar!!.progress = 0
            //mediaPlayer.seekTo(seekBar.getId());
            //seekBar.setMax(mediaPlayer.getDuration());
            val fis = FileInputStream(temp3GP)
            mediaPlayer!!.setDataSource(fis.fd)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
            handler!!.post(UpdatePlayTime)
            mediaPlayer!!.setOnCompletionListener {
                imgbtn_record!!.setBackgroundResource(R.drawable.playbutton)
                finish()
            }
        } catch (ex: IOException) {
            val s = ex.toString()
            ex.printStackTrace()
        }
    }

    var UpdateRecordTime: Runnable = object : Runnable {
        override fun run() {
            if (isRecording!!) {
                recordTime += 1
                // Delay 1s before next call
                handler!!.postDelayed(this, 1000)
                chronometer!!.start()
            }
        }
    }
    var UpdatePlayTime: Runnable = object : Runnable {
        override fun run() {
            if (mediaPlayer!!.isPlaying) {

                // Update play time and SeekBar
                playTime += 1
                //seekBar.setProgress(playTime);
                seekBar!!.max = mediaPlayer!!.duration
                seekBar!!.progress = mediaPlayer!!.currentPosition

                // Delay 1s before next call
                handler!!.postDelayed(this, 1000)
            }
        }
    }

    @Throws(IOException::class)
    fun convert(path: String?): ByteArray {
        val fis = FileInputStream(path)
        val bos = ByteArrayOutputStream()
        val b = ByteArray(1024)
        var readNum: Int
        while (fis.read(b).also { readNum = it } != -1) {
            bos.write(b, 0, readNum)
        }
        return bos.toByteArray()
    }
}