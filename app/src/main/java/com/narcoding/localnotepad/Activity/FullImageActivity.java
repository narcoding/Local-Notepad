package com.narcoding.localnotepad.Activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.narcoding.localnotepad.R;

public class FullImageActivity extends AppCompatActivity {
    private ImageView img_fullScreen;
    private RelativeLayout rl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        img_fullScreen= (ImageView) findViewById(R.id.img_fullScreen);
        rl= (RelativeLayout) findViewById(R.id.rl_activity_full_image);

        byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        img_fullScreen.setImageBitmap(bmp);
        //BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bmp);
        //rl.setBackgroundDrawable(bitmapDrawable);

    }

}
