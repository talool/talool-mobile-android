package com.talool.android.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by dmccuen on 8/1/14.
 */
public abstract class TaloolDatePicker extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private String title;

    protected TaloolDatePicker(String title) {
        this.title = title;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        Dialog d = new DatePickerDialog(getActivity(), this, year, month, day);

        d.setTitle(title);

        return d;
    }

    abstract public void onDateSet(DatePicker view, int year, int month, int day);
}