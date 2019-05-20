package com.project.harrisonturley.whiteboardide;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.kbiakov.codeview.CodeView;
import io.github.kbiakov.codeview.adapters.Options;
import io.github.kbiakov.codeview.highlight.ColorTheme;

public class CodeOutput extends AppCompatActivity {

    private static final String CODE_LINES_TAG = "CodeLines";
    private static final String CODE_OUTPUT_TAG = "CodeOutput";

    private ArrayList<String> codeText;
    private String codeOutput;

    private TextView outputTextView;
    private CodeView codeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_output);

        codeText = (ArrayList<String>)getIntent().getSerializableExtra(CODE_LINES_TAG);
        codeOutput = (String)getIntent().getExtras().getString(CODE_OUTPUT_TAG);

        outputTextView = findViewById(R.id.output_text);
        codeView = findViewById(R.id.output_code_view);

        setupCodeView();
        setupOutputText();
    }

    public void onClickBack(View v) {

    }

    private void setupCodeView() {
        String lines = getCodeStringFromLines();
        codeView.setOptions(Options.Default.get(this).withTheme(ColorTheme.MONOKAI));
        codeView.setCode(lines);
    }

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

        for (int i = 0; i < codeText.size(); i++) {
            code += codeText.get(i);
            code += "\n";
        }

        return code;
    }
}
