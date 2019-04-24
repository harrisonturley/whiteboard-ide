package com.project.harrisonturley.whiteboardide;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.github.kbiakov.codeview.CodeView;
import io.github.kbiakov.codeview.adapters.Options;
import io.github.kbiakov.codeview.highlight.ColorTheme;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, HttpRequestClient.HttpResponseListener {

    private HttpRequestClient mHttpRequestClient;
    private CodeView codeView;

    private static final String uriBase = "https://westus.api.cognitive.microsoft.com/vision/v2.0/recognizeText";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHttpRequestClient = new HttpRequestClient(getString(R.string.image_processing_api_key), this);
        codeView = (CodeView) findViewById(R.id.code_view);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            sendImage(extras.getString(getString(R.string.saved_image_path)));
        } else {

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /* HttpRequestClient callbacks */

    public void onImageProcessingResponse(JSONObject response) {
        try {
            if (!response.getString("status").equals("Succeeded"))
                return;

            JSONArray results = response.getJSONObject("recognitionResult").getJSONArray("lines");
            final String[] resultStrings = new String[results.length()];

            for (int i = 0; i < results.length(); i++) {
                JSONObject tempResult = results.getJSONObject(i);
                resultStrings[i] = tempResult.getString("text");
            }

            Log.e("FinalizedText", Arrays.toString(resultStrings));
            onNewCodeReceived(resultStrings);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onClickCamera(View v) {
        Intent intent = new Intent(this, PictureActivity.class);
        startActivity(intent);
    }

    private void onNewCodeReceived(final String[] lines) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                codeView.setCode(Arrays.toString(lines));
            }
        });
    }

    private void sendImage(String imageFilePath) {
        Map<String, Object> params = new HashMap<>();
        File imgFile = new File(imageFilePath);

        params.put("mode", "Handwritten");
        String url = HttpRequestClient.getUrl(uriBase, params);

        codeView.setOptions(Options.Default.get(this)
            .withTheme(ColorTheme.MONOKAI));

        try {
            InputStream imgStream = new FileInputStream(imgFile);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int bytesRead;
            byte[] bytes = new byte[1024];
            while ((bytesRead = imgStream.read(bytes)) > 0) {
                byteArrayOutputStream.write(bytes, 0, bytesRead);
            }

            byte[] data = byteArrayOutputStream.toByteArray();
            params.put("data", data);

            mHttpRequestClient.postWriting(url, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
