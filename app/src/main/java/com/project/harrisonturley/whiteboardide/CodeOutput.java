package com.project.harrisonturley.whiteboardide;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.googlejavaformat.java.Formatter;

import java.util.ArrayList;

import io.github.kbiakov.codeview.CodeView;
import io.github.kbiakov.codeview.adapters.Options;
import io.github.kbiakov.codeview.highlight.ColorTheme;

/**
 * Screen for displaying code and it's output after being run
 */
public class CodeOutput extends AppCompatActivity {

    private ArrayList<String> codeText;
    private String codeOutput;

    private TextView outputTextView;
    private CodeView codeView;

    /**
     * Sets up the output screen, initializes fields for the activity
     *
     * @param savedInstanceState activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_output);

        codeText = (ArrayList<String>)getIntent().getSerializableExtra(getString(R.string.code_lines_tag));
        codeOutput = (String)getIntent().getExtras().getString(getString(R.string.code_output_tag));

        outputTextView = findViewById(R.id.output_text);
        codeView = findViewById(R.id.output_code_view);

        setupCodeView();
        setupOutputText();
    }

    /**
     * Overrides hardware back button to call software back button
     */
    @Override
    public void onBackPressed() {
        onClickBack(null);
    }

    /**
     * Returns to the main activity with the code as a parameter
     *
     * @param v button that was selected
     */
    public void onClickBack(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(getString(R.string.code_lines_tag), codeText);
        startActivity(intent);
    }

    /**
     * Sets up the code view to display the provided code
     */
    private void setupCodeView() {
        String lines = getCodeStringFromLines();
        codeView.setOptions(Options.Default.get(this).withTheme(ColorTheme.MONOKAI));
        codeView.setCode(lines);
    }

    /**
     * Sets up the output text window to display the results of the code being run
     */
    private void setupOutputText() {
        outputTextView.setText(codeOutput);
    }

    /**
     * Translates the code text list to a string format
     *
     * @return string of code
     */
    private String getCodeStringFromLines() {
        String code = "";
        String formattedCode;

        for (int i = 0; i < codeText.size(); i++) {
            code += codeText.get(i);
            code += "\n";
        }

        try {
            formattedCode = new Formatter().formatSource(code);
            return formattedCode;
        } catch (Exception e) {
            e.printStackTrace();
            return code;
        }
    }
}
