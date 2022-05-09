package pwr.edu.app.parkinsonsdisease.testimplementation.gyroscopetest;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import pwr.edu.app.parkinsonsdisease.R;
import pwr.edu.app.parkinsonsdisease.mbientlab.DeviceMainActivity;

public class GyroscopeTest extends AppCompatActivity {
    public final static String HOUR_SINCE_LAST_DRUG= "hour since last drug";
    public final static String NAME_TEST = "NAME_TEST";
    private final static String GYROSCOPE_TEST = "GYROSCOPE_TEST";

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private EditText timeLastDrug;
    private Button goToTestBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_drug);

        timeLastDrug = findViewById(R.id.lastDrugEditText);
        goToTestBtn = findViewById(R.id.goToTestButton);
        goToTestBtn.setOnClickListener(v -> onClickButton());
    }

    private void onClickButton() {
        if(!timeLastDrug.getText().toString().isEmpty()){
            String hoursSinceLastMed = timeLastDrug.getText().toString();
            Intent intent = new Intent(this, DeviceMainActivity.class);
            intent.putExtra(HOUR_SINCE_LAST_DRUG, hoursSinceLastMed);
            intent.putExtra(NAME_TEST, GYROSCOPE_TEST);
            startActivity(intent);
            finish();
        }else{
            Toast.makeText(this, R.string.no_data,  Toast.LENGTH_SHORT).show();
        }
    }
}