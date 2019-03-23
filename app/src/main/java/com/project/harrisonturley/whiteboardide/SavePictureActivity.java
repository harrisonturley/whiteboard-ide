package com.project.harrisonturley.whiteboardide;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class SavePictureActivity extends AppCompatActivity {

    private ImageView savedImage;
    private String savedImagePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_picture);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            savedImagePath = extras.getString(getString(R.string.saved_image_path));
        }

        savedImage = (ImageView)findViewById(R.id.saved_image);
        setupSavedImage();
    }

    public void onClickSend(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(getString(R.string.saved_image_path), savedImagePath);
        startActivity(intent);
    }

    public void onClickCancel(View v) {
        Intent intent = new Intent(this, PictureActivity.class);
        startActivity(intent);
    }

    private void setupSavedImage() {
        if (savedImagePath == null) {
            Log.e("Image Path", "Error getting image path");
        }

        File imageFile = new File(savedImagePath);
        if (imageFile.exists()) {
            Bitmap imgBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            savedImage.setImageBitmap(imgBitmap);
        }
    }
}
