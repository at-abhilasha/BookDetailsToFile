package com.inventory.kp.dialogs;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by abhilasha.jain on 1/6/2016.
 */
public class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public interface DatePickerDialogListener {
        void onDateSet(int year, int month, int date);
    }

    DatePickerDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public  void onAttach(Activity activity) {
        super.onAttach(activity);
        try  {
            mListener = (DatePickerDialogListener) activity ;
        }  catch  ( ClassCastException e )  {
            throw  new  ClassCastException(activity.toString() + "MUST Implement DatePickerDialogListener");
        }
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        mListener.onDateSet(year, month, day);
    }
}

