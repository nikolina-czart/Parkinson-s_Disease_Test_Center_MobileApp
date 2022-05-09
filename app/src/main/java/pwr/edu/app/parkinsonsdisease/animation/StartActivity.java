package pwr.edu.app.parkinsonsdisease.animation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.FragmentActivity;

import pwr.edu.app.parkinsonsdisease.authorization.LoginActivity;
import pwr.edu.app.parkinsonsdisease.R;

public class StartActivity extends FragmentActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animation_splash_start);

        handler.postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }, 3000);
    }

}
