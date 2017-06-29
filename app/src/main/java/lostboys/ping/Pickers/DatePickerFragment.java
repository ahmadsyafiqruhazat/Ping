package lostboys.ping.Pickers;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;

/**
 * Created by Syafiq on 29/6/2017.
 */
public class DatePickerFragment extends DialogFragment
         {
             private Activity mActivity;
             private DatePickerDialog.OnDateSetListener mListener;
             int hour,minute;
             @Override
             public void onAttach(Activity activity) {
                 super.onAttach(activity);
                 mActivity = activity;

                 // This error will remind you to implement an OnTimeSetListener
                 //   in your Activity if you forget
                 try {
                     mListener = (DatePickerDialog.OnDateSetListener) activity;
                 } catch (ClassCastException e) {
                     throw new ClassCastException(activity.toString() + " must implement OnTimeSetListener");
                 }
             }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(mActivity, mListener, year, month, day);
    }
}