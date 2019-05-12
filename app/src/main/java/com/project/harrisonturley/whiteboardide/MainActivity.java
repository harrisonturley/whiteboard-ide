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
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.github.kbiakov.codeview.CodeView;
import io.github.kbiakov.codeview.OnCodeLineClickListener;
import io.github.kbiakov.codeview.adapters.Options;
import io.github.kbiakov.codeview.highlight.ColorTheme;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements HttpRequestClient.HttpResponseListener, CodeLineEditFragment.CodeLineEditListener {

    private static final String uriBase = "https://westus.api.cognitive.microsoft.com/vision/v2.0/recognizeText";

    private HttpRequestClient mHttpRequestClient;
    private CodeView codeView;

    private ArrayList<String> codeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHttpRequestClient = new HttpRequestClient(getString(R.string.image_processing_api_key), this);
        codeView = (CodeView) findViewById(R.id.code_view);
        codeText = new ArrayList<String>();

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            sendImage(extras.getString(getString(R.string.saved_image_path)));
        } else {
            String[] testCode = {"package io.github.kbiakov.codeviewexample;",
                    "",
                    "import android.os.Bundle;",
                    "import android.support.annotation.Nullable;",
                    "import android.support.v7.app.AppCompatActivity;",
                    "import android.util.Log;",
                    "",
                    "import org.jetbrains.annotations.NotNull;"};
            codeText = new ArrayList(Arrays.asList(testCode));
            newCodeReceived();
        }

        codeView.getOptions().addCodeLineClickListener(new OnCodeLineClickListener() {
            @Override
            public void onCodeLineClicked(int i, @NotNull String s) {
                openLineEditDialog(i, s);
            }
        });
    }

    @Override
    public void onBackPressed() { }

    /*
     * HttpRequestClient callbacks
     */

    public void onImageProcessingResponse(JSONObject response) {
        try {
            if (!response.getString("status").equals("Succeeded")) {
                fireNoResultToast();
                return;
            }

            JSONArray results = response.getJSONObject("recognitionResult").getJSONArray("lines");
            codeText.clear();

            for (int i = 0; i < results.length(); i++) {
                JSONObject tempResult = results.getJSONObject(i);
                codeText.add(tempResult.getString("text"));
            }

            newCodeReceived();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
     * CodeLineEditFragment listener callbacks
     */

    public void onLineSaved(int line, String newText) {
        codeText.set(line, newText);
        newCodeReceived();
    }

    public void onClickCamera(View v) {
        Intent intent = new Intent(this, PictureActivity.class);
        startActivity(intent);
    }

    private void newCodeReceived() {
        final String lines = getCodeStringFromLines();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                codeView.setCode(lines);
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

    private void openLineEditDialog(int line, String text) {
        CodeLineEditFragment lineFragment = CodeLineEditFragment.newInstance(line, text);
        lineFragment.show(getFragmentManager(), "LineEdit");
    }

    private String getCodeStringFromLines() {
        String code = "";

        for (int i = 0; i < codeText.size(); i++) {
            code += codeText.get(i);
            code += "\n";
        }

        return code;
    }

    private void fireNoResultToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Failed to get text from image!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
