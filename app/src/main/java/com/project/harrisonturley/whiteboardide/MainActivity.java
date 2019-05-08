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
//            codeText = new ArrayList<String>();
            onNewCodeReceived("package io.github.kbiakov.codeviewexample;\n" +
                    "\n" +
                    "import android.os.Bundle;\n" +
                    "import android.support.annotation.Nullable;\n" +
                    "import android.support.v7.app.AppCompatActivity;\n" +
                    "import android.util.Log;\n" +
                    "\n" +
                    "import org.jetbrains.annotations.NotNull;\n" +
                    "\n" +
                    "import io.github.kbiakov.codeview.CodeView;\n" +
                    "import io.github.kbiakov.codeview.OnCodeLineClickListener;\n" +
                    "import io.github.kbiakov.codeview.adapters.CodeWithDiffsAdapter;\n" +
                    "import io.github.kbiakov.codeview.adapters.Options;\n" +
                    "import io.github.kbiakov.codeview.highlight.ColorTheme;\n" +
                    "import io.github.kbiakov.codeview.highlight.ColorThemeData;\n" +
                    "import io.github.kbiakov.codeview.highlight.Font;\n" +
                    "import io.github.kbiakov.codeview.highlight.FontCache;\n" +
                    "import io.github.kbiakov.codeview.views.DiffModel;\n" +
                    "\n" +
                    "public class ListingsActivity extends AppCompatActivity {\n" +
                    "\n" +
                    "    @Override\n" +
                    "    protected void onCreate(@Nullable Bundle savedInstanceState) {\n" +
                    "        super.onCreate(savedInstanceState);\n" +
                    "        setContentView(R.layout.activity_listings);\n" +
                    "\n" +
                    "        final CodeView codeView = (CodeView) findViewById(R.id.code_view);\n" +
                    "\n" +
                    "        /*\n" +
                    "         * 1: set code content\n" +
                    "         */\n" +
                    "\n" +
                    "        // auto language recognition\n" +
                    "        codeView.setCode(getString(R.string.listing_js));\n" +
                    "\n" +
                    "        // specify language for code listing\n" +
                    "        codeView.setCode(getString(R.string.listing_py), \"py\");\n" +
                    "    }\n" +
                    "}");
        }

        codeView.getOptions().addCodeLineClickListener(new OnCodeLineClickListener() {
            @Override
            public void onCodeLineClicked(int i, @NotNull String s) {
                openLineEditDialog(i);
            }
        });
    }

    @Override
    public void onBackPressed() { }

    //
    // HttpRequestClient callbacks
    //

    public void onImageProcessingResponse(JSONObject response) {
        try {
            if (!response.getString("status").equals("Succeeded"))
                return;

            JSONArray results = response.getJSONObject("recognitionResult").getJSONArray("lines");
            final String[] resultStrings = new String[results.length()];
            codeText.clear();

            for (int i = 0; i < results.length(); i++) {
                JSONObject tempResult = results.getJSONObject(i);
                resultStrings[i] = tempResult.getString("text");
                codeText.add(tempResult.getString("text"));
            }

            Log.e("FinalizedText", Arrays.toString(resultStrings));
            //onNewCodeReceived(resultStrings);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //
    // CodeLineEditFragment listener callbacks
    //

    public void onLineSaved(int line, String newText) {

    }

    public void onClickCamera(View v) {
        Intent intent = new Intent(this, PictureActivity.class);
        startActivity(intent);
    }

    private void onNewCodeReceived(final String lines) {
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

    private void openLineEditDialog(int line) {
        CodeLineEditFragment lineFragment = new CodeLineEditFragment();
        lineFragment.show(getFragmentManager(), "LineEdit");
    }
}
