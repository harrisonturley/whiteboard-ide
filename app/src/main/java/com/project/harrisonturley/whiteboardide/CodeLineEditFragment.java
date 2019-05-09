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

/**
 * Dialog fragment to allow editing of individual lines of code, with callback for results to code view
 */
public class CodeLineEditFragment extends DialogFragment {

    /**
     * Callback from saving the code to update code view
     */
    public interface CodeLineEditListener {
        void onLineSaved(int line, String newText);
    }

    private CodeLineEditListener listener;

    /**
     * Creates the dialog fragment based on the layout
     * @param savedInstanceState saved state variables
     * @return the built dialog
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_code_line_edit, null);

        builder.setView(v)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CodeLineEditFragment.this.getDialog().cancel();
                    }
                }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onLineSaved(1, "test");
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
