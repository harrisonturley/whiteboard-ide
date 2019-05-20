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
import android.widget.ProgressBar;
import android.widget.TextView;
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

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements HttpRequestClient.HttpResponseListener, CodeLineEditFragment.CodeLineEditListener {

    private static final String IMAGE_URI_BASE = "https://westus.api.cognitive.microsoft.com/vision/v2.0/recognizeText";
    private static final String CODE_EXECUTE_URI_BASE = "https://api.jdoodle.com/v1/execute";
    private static final int CODE_SUCCESS_STATUS = 200;

    private HttpRequestClient mHttpRequestClient;
    private ArrayList<String> codeText;

    private CodeView codeView;
    private ProgressBar loadingSpinner;
    private TextView progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHttpRequestClient = new HttpRequestClient(getString(R.string.image_processing_api_key), getString(R.string.jdoodle_client_id), getString(R.string.jdoodle_client_secret), this);
        codeText = new ArrayList<String>();

        codeView = (CodeView) findViewById(R.id.code_view);
        loadingSpinner = (ProgressBar) findViewById(R.id.loading_progress_spinner);
        progressText = (TextView) findViewById(R.id.progress_spinner_text);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            sendImage(extras.getString(getString(R.string.saved_image_path)));
            loadingSpinner.setVisibility(VISIBLE);
            progressText.setVisibility(VISIBLE);
            codeView.setVisibility(GONE);
        } else {
            loadingSpinner.setVisibility(GONE);
            progressText.setVisibility(GONE);
            String[] testCode = {"public class MyClass {",
                    "",
                    "    public static void main(String args[]) {",
                    "        int x = 10;",
                    "        int y = 35;",
                    "",
                    "        System.out.println(\"Sum of x+y = \" + (x + y));",
                    "    }",
                    "}"};
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

    public void onCodeRunResponse(String response, int statusCode) {
        if (statusCode == CODE_SUCCESS_STATUS) {
            // Change to new activity here with response in bundle
            Intent intent = new Intent(this, CodeOutput.class);
            startActivity(intent);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Failed to run code!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /*
     * CodeLineEditFragment listener callbacks
     */

    public void onLineChanged(int line, String newText) {
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
                loadingSpinner.setVisibility(GONE);
                progressText.setVisibility(GONE);
                codeView.setVisibility(VISIBLE);
                codeView.setCode(lines);
            }
        });
    }

    private void sendImage(String imageFilePath) {
        Map<String, Object> params = new HashMap<>();
        File imgFile = new File(imageFilePath);

        params.put("mode", "Handwritten");
        String url = HttpRequestClient.getUrl(IMAGE_URI_BASE, params);

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

            mHttpRequestClient.postImage(url, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickPlayCode(View v) {
        mHttpRequestClient.postCode(CODE_EXECUTE_URI_BASE, getCodeStringFromLines());
    }

    private void openLineEditDialog(int line, String text) {
        CodeLineEditFragment lineFragment = CodeLineEditFragment.newInstance(line, text);
        lineFragment.show(getFragmentManager(), "LineEdit");
    }

    /**
     * Translates the code text list to a string format
     *
     * @return string of code
     */
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
