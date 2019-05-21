package com.project.harrisonturley.whiteboardide;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Dialog fragment to allow editing of individual lines of code, with callback for results to code view
 */
public class CodeLineEditFragment extends DialogFragment {

    /**
     * Callback from saving the code to update code view
     */
    public interface CodeLineEditListener {
        void onLineChanged(int line, String newText);
        void onAddLine(int line);
        void onDeleteLine(int line);
    }

    private static final String LINE_ARG = "LineNum";
    private static final String CODE_ARG = "CurrentText";

    private EditText codeEntryField;
    private TextView titleText;
    private ImageView addLineButton;
    private ImageView deleteLineButton;

    private CodeLineEditListener listener;

    public static CodeLineEditFragment newInstance(int line, String currentText) {
        CodeLineEditFragment newLineDialog = new CodeLineEditFragment();

        Bundle args = new Bundle();
        args.putInt(LINE_ARG, line);
        args.putString(CODE_ARG, currentText);
        newLineDialog.setArguments(args);

        return newLineDialog;
    }

    /**
     * Creates the dialog fragment based on the layout
     * @param savedInstanceState saved state variables
     * @return the built dialog
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int lineNum = getArguments().getInt(LINE_ARG);
        String currentCode = getArguments().getString(CODE_ARG);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_code_line_edit, null);

        codeEntryField = v.findViewById(R.id.code_entry_field);
        titleText = v.findViewById(R.id.code_entry_title);
        addLineButton = v.findViewById(R.id.new_line_icon);
        deleteLineButton = v.findViewById(R.id.delete_line_icon);

        codeEntryField.setText(currentCode);
        titleText.setText("Line " + (lineNum + 1));

        addLineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onAddLine(lineNum);
                CodeLineEditFragment.this.getDialog().cancel();
            }
        });

        deleteLineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteLine(lineNum);
                CodeLineEditFragment.this.getDialog().cancel();
            }
        });

        builder.setView(v)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CodeLineEditFragment.this.getDialog().cancel();
                    }
                }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onLineChanged(lineNum, codeEntryField.getText().toString());
                        CodeLineEditFragment.this.getDialog().cancel();
                    }
        });

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.brightBlue));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.brightBlue));
            }
        });

        return dialog;
    }

    /**
     * Sets up the dialog to new design
     */
    @Override
    public void onStart(){
        super.onStart();

        AlertDialog dialog = (AlertDialog)getDialog();
        Window window = dialog.getWindow();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.brightBlue, getActivity().getTheme()));
        window.setBackgroundDrawableResource(R.drawable.background_rounded);
    }

    /**
     * Ensures that the context that created the line editing dialog implements the CodeLineEditListener
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (CodeLineEditListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Must implement CodeLineEditListener");
        }
    }
}
