package lostboys.ping;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;


/**
 * Created by Syafiq on 23/6/2017.
 */

public class EventCreate extends AppCompatActivity {
    private DatabaseReference mDatabase;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createevent);

    }
}
