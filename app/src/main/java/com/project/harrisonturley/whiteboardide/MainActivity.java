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
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.googlejavaformat.java.Formatter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openjdk.tools.javac.util.StringUtils;

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

/**
 * Screen to edit/run code, launch camera, and select language
 */
public class MainActivity extends AppCompatActivity implements HttpRequestClient.HttpResponseListener, CodeLineEditFragment.CodeLineEditListener {

    private static final String IMAGE_URI_BASE = "https://westus.api.cognitive.microsoft.com/vision/v2.0/recognizeText";
    private static final String CODE_EXECUTE_URI_BASE = "https://api.jdoodle.com/v1/execute";
    private static final int CODE_SUCCESS_STATUS = 200;

    private HttpRequestClient mHttpRequestClient;
    private ArrayList<String> codeText;

    private CodeView codeView;
    private ProgressBar loadingSpinner;
    private TextView progressText;
    private Spinner languageSpinner;

    /**
     * Sets up the main screen, based on the Android lifecycle.  Initializes all fields for the class.
     *
     * @param savedInstanceState activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle extras = getIntent().getExtras();

        mHttpRequestClient = new HttpRequestClient(getString(R.string.image_processing_api_key), getString(R.string.jdoodle_client_id), getString(R.string.jdoodle_client_secret), this);
        codeText = new ArrayList<String>();

        codeView = findViewById(R.id.code_view);
        loadingSpinner = findViewById(R.id.loading_progress_spinner);
        progressText = findViewById(R.id.progress_spinner_text);
        languageSpinner = findViewById(R.id.language_spinner);

        codeView.setOptions(Options.Default.get(this).withTheme(ColorTheme.MONOKAI));
        codeView.getOptions().addCodeLineClickListener(new OnCodeLineClickListener() {
            @Override
            public void onCodeLineClicked(int i, @NotNull String s) {
                openLineEditDialog(i, s);
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.language_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        if (extras != null) {
            if (extras.getString(getString(R.string.saved_image_path)) != null) {
                loadingSpinner.setVisibility(VISIBLE);
                progressText.setVisibility(VISIBLE);
                codeView.setVisibility(GONE);
                sendImage(extras.getString(getString(R.string.saved_image_path)));
            } else {
                loadingSpinner.setVisibility(GONE);
                progressText.setVisibility(GONE);
                codeView.setVisibility(VISIBLE);
                codeText = (ArrayList<String>)getIntent().getSerializableExtra(getString(R.string.code_lines_tag));
                newCodeReceived();
            }
        } else {
            loadingSpinner.setVisibility(GONE);
            progressText.setVisibility(GONE);
            String[] defaultCode = {"public class MyClass {",
                    "",
                    "    public static void main(String args[]) {",
                    "        int x = 40;",
                    "        int y = 35;",
                    "        int z = 3;",
                    "",
                    "        System.out.println(\"Sum of (x+y)/z = \" + (x + y)/z);",
                    "    }",
                    "}"};
            codeText = new ArrayList(Arrays.asList(defaultCode));
            newCodeReceived();
        }
    }

    /**
     * Overrides back button functionality such that user can't accidentally break navigation
     */
    @Override
    public void onBackPressed() { }


    // HttpRequestClient callbacks //

    /**
     * Callback from HttpRequestClient, with the response from processing the image of code
     *
     * @param response json object response from Azure's Computer Vision API
     */
    public void onImageProcessingResponse(JSONObject response) {
        try {
            if (!response.getString("status").equals("Succeeded")) {
                fireNoResultToast();
                return;
            }

            JSONArray results = response.getJSONObject("recognitionResult").getJSONArray("lines");
            codeText.clear();

            if (results == null) {
                fireNoResultToast();
                return;
            }

            for (int i = 0; i < results.length(); i++) {
                JSONObject tempResult = results.getJSONObject(i);
                codeText.add(tempResult.getString("text"));
            }

            newCodeReceived();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Callback from HttpRequestClient, with response from running the code
     *
     * @param response string of response from Jdoodle's API
     * @param statusCode response code that Jdoodle sent, 200 == success
     */
    public void onCodeRunResponse(String response, int statusCode) {
        if (statusCode == CODE_SUCCESS_STATUS) {
            Intent intent = new Intent(this, CodeOutput.class);
            intent.putExtra(getString(R.string.code_lines_tag), codeText);
            intent.putExtra(getString(R.string.code_output_tag), response);
            startActivity(intent);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingSpinner.setVisibility(GONE);
                    progressText.setVisibility(GONE);
                    languageSpinner.setClickable(true);
                    Toast.makeText(MainActivity.this, "Failed to run code! Check language setting.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    // CodeLineEditFragment listener callbacks //

    /**
     * Callback from CodeLineEditFragment, notifying main activity to update code when a line is changed
     *
     * @param line number of line changed
     * @param newText text for the provided line
     */
    public void onLineChanged(int line, String newText) {
        codeText.set(line, newText);
        newCodeReceived();
    }

    /**
     * Callback from CodeLineEditFragment, notifying main activity to add a line below the selected line number
     *
     * @param line number of line to add another line below
     */
    public void onAddLine(int line) {
        if (line >= codeText.size()) {
            codeText.add(line, "");
        } else {
            codeText.add(line + 1, "");
        }

        newCodeReceived();
    }

    /**
     * Callback from the CodeLineEditFragment, notifying main activity to delete the provided line
     *
     * @param line number of line to delete
     */
    public void onDeleteLine(int line) {
        codeText.remove(line);
        newCodeReceived();
    }


    // Helpers //

    /**
     * Starts camera activity when user selects button
     *
     * @param v view selected
     */
    public void onClickCamera(View v) {
        Intent intent = new Intent(this, PictureActivity.class);
        startActivity(intent);
    }

    /**
     * Updates the code view.  To be called whenever new lines of code are received or modified
     */
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

    /**
     * Sends the image at the provided path to the Azure Computer Vision API to be processed for text/code
     *
     * @param imageFilePath path to image
     */
    private void sendImage(String imageFilePath) {
        Map<String, Object> params = new HashMap<>();
        File imgFile = new File(imageFilePath);

        params.put("mode", "Handwritten");
        String url = HttpRequestClient.getUrl(IMAGE_URI_BASE, params);

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

    /**
     * Runs the code in main activity with Jdoodle's API.
     *
     * @param v view of button selected
     */
    public void onClickPlayCode(View v) {
        loadingSpinner.setVisibility(VISIBLE);
        progressText.setVisibility(VISIBLE);
        languageSpinner.setClickable(false);
        mHttpRequestClient.postCode(CODE_EXECUTE_URI_BASE, getCodeStringFromLines(), languageSpinner.getSelectedItem().toString());
    }

    /**
     * Opens the edit fragment for the given line of code
     *
     * @param line number of line that was selected for editing
     * @param text current text at selected line
     */
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
        String formattedCode;
        String code = "";

        for (int i = 0; i < codeText.size(); i++) {
            code += codeText.get(i);

            if (i != codeText.size() - 1) {
                code += "\n";
            }
        }

        try {
            formattedCode = new Formatter().formatSource(code);
            updateCodeTextFromString(formattedCode);
            return formattedCode;
        } catch (Exception e) {
            e.printStackTrace();
            return code;
        }
    }

    /**
     * Sets the code text ArrayList based a newly formatted string
     *
     * @param code new code to be parsed and set as the code text list
     */
    private void updateCodeTextFromString(String code) {
        String[] codeLines = code.split("\n");
        codeText = new ArrayList<>(Arrays.asList(codeLines));
        codeText.add(codeText.size(), "");
    }

    /**
     * Fires a "no results" toast on the UI thread
     */
    private void fireNoResultToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingSpinner.setVisibility(GONE);
                progressText.setVisibility(GONE);
                Toast.makeText(MainActivity.this, "Failed to get text from image!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
