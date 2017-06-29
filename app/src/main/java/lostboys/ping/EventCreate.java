package lostboys.ping;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import lostboys.ping.Models.EventEntry;
import lostboys.ping.Pickers.TimePickerFragment;


/**
 * Created by Syafiq on 23/6/2017.
 */

public class EventCreate extends FragmentActivity {
    private DatabaseReference mDatabase;
    private DatabaseReference eventCloudEndPoint;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private EditText name;
    private EditText date;
    private EditText time;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createevent);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase =  FirebaseDatabase.getInstance().getReference("users");
        eventCloudEndPoint =  mDatabase.child(
                mFirebaseUser.getUid()).child("events");
        EventEntry event = new EventEntry("test2",10.0);
        eventCloudEndPoint.setValue(event).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("event", e.getLocalizedMessage());
            }
        });

    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

}
