package com.project.harrisonturley.whiteboardide;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

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

    private void setupSavedImage() {
        if (savedImagePath == null) {
            Log.e("Image Path", "Error getting image path");
        }


    }
}
