package lostboys.ping.Pickers;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import lostboys.ping.R;

/**
 * Created by Asus on 16-Jul-17.
 */

public class Feedback extends Activity {

    private EditText feedback;
    private Button submit;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
    }
}


