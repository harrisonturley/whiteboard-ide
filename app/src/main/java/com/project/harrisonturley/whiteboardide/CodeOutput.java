package com.project.harrisonturley.whiteboardide;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.kbiakov.codeview.CodeView;
import io.github.kbiakov.codeview.adapters.Options;
import io.github.kbiakov.codeview.highlight.ColorTheme;

public class CodeOutput extends AppCompatActivity {

    private ArrayList<String> codeText;
    private String codeOutput;

    private TextView outputTextView;
    private CodeView codeView;

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

    public void onClickBack(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(getString(R.string.code_lines_tag), codeText);
        startActivity(intent);
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
