package pwr.edu.app.parkinsonsdisease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import pwr.edu.app.parkinsonsdisease.adapter.DoctorListAdapter;
import pwr.edu.app.parkinsonsdisease.adapter.ParkinsonTestAdapter;
import pwr.edu.app.parkinsonsdisease.entity.Doctor;
import pwr.edu.app.parkinsonsdisease.entity.ParkinsonTest;
import pwr.edu.app.parkinsonsdisease.mbientlab.DeviceMainActivity;
import pwr.edu.app.parkinsonsdisease.mbientlab.DeviceSetupActivity;
import pwr.edu.app.parkinsonsdisease.registration.CreateNewDoctor;

public class AdminMainActivity extends AppCompatActivity {
    private Button btnNewDoctor;
    private DoctorListAdapter adapter;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference doctorReference = db.collection("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);
        init();

        Button btnExit = (Button) findViewById(R.id.btnExit);
        btnExit.setOnClickListener(v -> {
            finish();
            System.exit(0);
        });

        btnNewDoctor.setOnClickListener(v -> onClickButton());

        setUpRecycleView();
    }

    private void onClickButton() {
        Intent intent = new Intent(this, CreateNewDoctor.class);
        startActivity(intent);
    }

    private void setUpRecycleView() {
        Query query = doctorReference.whereEqualTo("role", "DOCTOR");

        FirestoreRecyclerOptions<Doctor> options = new FirestoreRecyclerOptions.Builder<Doctor>()
                .setQuery(query, Doctor.class)
                .build();

        adapter = new DoctorListAdapter(options, getApplicationContext());

        RecyclerView recyclerView = findViewById(R.id.recycler_view_all_doctors);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void init() {
        btnNewDoctor = (Button) findViewById(R.id.btnAddNewDoctor);
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
