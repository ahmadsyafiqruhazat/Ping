package lostboys.ping;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import lostboys.ping.Models.Profile;

public class MainActivity extends Activity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    Bitmap pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_main);
        ProfilePictureView profilePictureView;

        profilePictureView = (ProfilePictureView) findViewById(R.id.friendProfilePicture);

        profilePictureView.setProfileId("447679082258221");
        mDatabase =  FirebaseDatabase.getInstance().getReference("users");

        firebaseAuth = FirebaseAuth.getInstance();
          user = firebaseAuth.getCurrentUser();
                if (user == null) {

                    goLoginScreen();
                }  else {
                    getCurrentUser();

                }
    }





    public void getCurrentUser(){
        Toast.makeText(getApplicationContext(),"loading",Toast.LENGTH_SHORT).show();

        mDatabase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Profile userProfile;
                userProfile = dataSnapshot.child(user.getUid()).getValue(Profile.class);
                SharedPreferences mPrefs = getSharedPreferences("myPrefs",MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(userProfile);
                prefsEditor.putString("User", json);
                prefsEditor.commit();

                Toast.makeText(getApplicationContext(),userProfile.userName,Toast.LENGTH_SHORT).show();
                goMap();



            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                goMap();

            }
        });
    }

    @Override
    protected void onRestart() {
        goMap();
        super.onRestart();
    }

    private void goMap() {
       Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    private void goLoginScreen() {
        Intent intent = new Intent(MainActivity.this, FacebookLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        goLoginScreen();
    }

}
