package lostboys.ping;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class Rewards extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        CircularProgressBar circularProgressBar = (CircularProgressBar)findViewById(R.id.prog);
        int animationDuration = 2500; // 2500ms = 2,5s
        circularProgressBar.setProgressWithAnimation(45, animationDuration); // Default duration = 1500ms
    }
}
