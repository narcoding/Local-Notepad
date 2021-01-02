package com.narcoding.localnotepad.view

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.narcoding.localnotepad.R

class FullImageActivity : AppCompatActivity() {
    private var img_fullScreen: ImageView? = null
    private var rl: RelativeLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_image)
        img_fullScreen = findViewById<View>(R.id.img_fullScreen) as ImageView
        rl = findViewById<View>(R.id.rl_activity_full_image) as RelativeLayout
        val byteArray = intent.getByteArrayExtra("image")
        val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
        img_fullScreen!!.setImageBitmap(bmp)
        //BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bmp);
        //rl.setBackgroundDrawable(bitmapDrawable);
    }
}