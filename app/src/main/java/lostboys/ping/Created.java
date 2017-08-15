package lostboys.ping;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import lostboys.ping.Models.EventEntry;
import lostboys.ping.Models.Profile;


public class Created extends AppCompatActivity {
    DatabaseReference mDatabase;
    //    Profile obj;
    ArrayList<EventEntry> events = new ArrayList<>();
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    Profile obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joined_events);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("profile").child("eventsCreated");

        mDatabase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                Toast.makeText(getApplicationContext(), "user loaded", Toast.LENGTH_SHORT).show();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    EventEntry post = postSnapshot.getValue(EventEntry.class);
                    events.add(post);
                    RecyclerView rv = (RecyclerView) findViewById(R.id.rv);
                    LinearLayoutManager llm = new LinearLayoutManager(Created.this);
                    rv.setLayoutManager(llm);

                    RVAdapter adapter = new RVAdapter(events);
                    rv.setAdapter(adapter);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

            }
        });

    }
}
