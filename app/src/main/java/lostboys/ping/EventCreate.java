package lostboys.ping;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import lostboys.ping.Models.EventEntry;
import lostboys.ping.Pickers.DatePickerFragment;
import lostboys.ping.Pickers.TimePickerFragment;


/**
 * Created by Syafiq on 23/6/2017.
 */

public class EventCreate extends FragmentActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private DatabaseReference mDatabase;
    private DatabaseReference eventCloudEndPoint;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private EditText name,des;
    private Button submit;
    private int pickerHour,pickerMin,pickerYear,pickerMonth,pickerDay;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createevent);
        name = (EditText)findViewById(R.id.eventName);
        des = (EditText)findViewById(R.id.eventDes);
        submit = (Button)findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mFirebaseAuth = FirebaseAuth.getInstance();
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                mDatabase =  FirebaseDatabase.getInstance().getReference("users");
                eventCloudEndPoint =  mDatabase.child(
                        mFirebaseUser.getUid()).child("events").child(name.getText().toString());
                EventEntry event = new EventEntry(name.getText().toString(), pickerHour,pickerMin,pickerYear,pickerMonth,pickerDay,des.getText().toString());

                eventCloudEndPoint.setValue(event).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("event", e.getLocalizedMessage());
                    }
                });
                finish();
            }
        });

    }

    public void showTimePickerDialog(View v) {
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        pickerHour = hourOfDay;
        pickerMin = minute;
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        pickerYear = year;
        pickerMonth = month;
        pickerDay= day;

    }

}
