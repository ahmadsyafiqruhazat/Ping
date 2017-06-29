package lostboys.ping;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
          FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    goLoginScreen();
                } else {
                    goMap();
                }

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
