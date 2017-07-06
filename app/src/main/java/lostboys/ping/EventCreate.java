package lostboys.ping;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import lostboys.ping.Models.EventEntry;
import lostboys.ping.Pickers.DatePickerFragment;
import lostboys.ping.Pickers.TimePickerFragment;

/**
 * Created by Syafiq on 23/6/2017.
 */

public class EventCreate extends FragmentActivity
        implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private DatabaseReference mDatabase;
    private DatabaseReference userCloudEndPoint;
    private DatabaseReference eventCloudEndPoint;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private EditText name,des;
    private Button submit;
    private double lon,lat;
    private int pickerHour,pickerMin,pickerYear,pickerMonth,pickerDay;
    int PLACE_PICKER_REQUEST = 1;

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
                eventCloudEndPoint = FirebaseDatabase.getInstance().getReference("events");
                String key = eventCloudEndPoint.push().getKey();
                userCloudEndPoint =  mDatabase.child(
                        mFirebaseUser.getUid()).child("events").child(key);
                ArrayList<String> members = new ArrayList<String>();
                members.add(mFirebaseUser.getUid());
                EventEntry event = new EventEntry(name.getText().toString(), pickerHour,pickerMin,
                        pickerYear,pickerMonth,pickerDay,des.getText().toString(),lat,lon,key, members);

                userCloudEndPoint.setValue(event).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("event", e.getLocalizedMessage());
                    }
                });
                eventCloudEndPoint.child(key).setValue(event).addOnFailureListener(new OnFailureListener() {
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

    public void showPlacePicker(View v) throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                LatLng selectedLocation = place.getLatLng();
                lat = selectedLocation.latitude;
                lon = selectedLocation.longitude;
            }
        }
    }
}
