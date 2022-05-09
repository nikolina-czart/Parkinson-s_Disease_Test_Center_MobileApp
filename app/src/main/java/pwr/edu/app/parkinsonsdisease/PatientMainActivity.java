package pwr.edu.app.parkinsonsdisease;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class PatientMainActivity extends AppCompatActivity {
    private TextView patientID;
    private TextView patientName;
    private Button btnNewTest;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference userReference = db.collection("users").document(auth.getUid());

    private static final String KEY_EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_patient);
        init();

        Button btnExit = (Button) findViewById(R.id.btnExit);
        btnExit.setOnClickListener(v -> {
            finish();
            System.exit(0);
        });

        btnNewTest.setOnClickListener(v -> onClickButton());
    }

    private void onClickButton() {
        Intent intent = new Intent(getApplicationContext(), ChooseTest.class);
        Log.d("LOG_MAIN", "Przycisk nowy test");
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        userReference.addSnapshotListener(this, this::getUserInformation);
    }

    private void getUserInformation(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Toast.makeText(PatientMainActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
            Log.d("LOG_MAIN", e.toString());
            return;
        }
        if (documentSnapshot.exists()) {
            String email = documentSnapshot.getString(KEY_EMAIL);
            String[] split = email.split("@");
            String name = split[0].toUpperCase();
            patientName.setText(name);
            String id = (String.valueOf(name.charAt(0)).concat(String.valueOf(name.charAt(3))));
            patientID.setText(id);
        }
    }

    private void init() {
        patientID = (TextView) findViewById(R.id.patientIDTextView);
        patientName = (TextView) findViewById(R.id.nameTextView);
        btnNewTest = (Button) findViewById(R.id.newTestButton);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout_action) {

            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Wylogowanie z aplikacji")
                    .setMessage("Czy napewno chcesz wylogować się z aplikacji?")
                    .setPositiveButton("Tak", (dialog, which) -> {
                        auth.signOut();
                        finish();
//                        System.exit(0);
                    })
                    .setNegativeButton("Nie", null)
                    .show();
        }
        return false;
    }
}