package pwr.edu.app.parkinsonsdisease;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pwr.edu.app.parkinsonsdisease.adapter.DoctorListAdapter;
import pwr.edu.app.parkinsonsdisease.adapter.ParkinsonTestSelectedListAdapter;
import pwr.edu.app.parkinsonsdisease.adapter.PatientListAdapter;
import pwr.edu.app.parkinsonsdisease.entity.Doctor;
import pwr.edu.app.parkinsonsdisease.entity.ParkinsonTest;
import pwr.edu.app.parkinsonsdisease.entity.Patient;
import pwr.edu.app.parkinsonsdisease.registration.CreateNewDoctor;
import pwr.edu.app.parkinsonsdisease.registration.CreateNewPatient;

public class DoctorMainActivity extends AppCompatActivity{
    private TextView etDoctorEmail;
    private Button btnNewPatient;
    private PatientListAdapter adapter;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference patientReference = db.collection("users");

    private DocumentReference userReference = db.collection("users").document(auth.getUid());

    private CollectionReference testReference = db.collection("tests");
    final ArrayList<ParkinsonTest> testListAll = new ArrayList();


    private static final String KEY_EMAIL = "doctorEmail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_doctor);
        init();

        Button btnExit = (Button) findViewById(R.id.btnExit);
        btnExit.setOnClickListener(v -> {
            finish();
            System.exit(0);
        });


        btnNewPatient.setOnClickListener(v -> onClickButton());

        setUpRecycleView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        userReference.addSnapshotListener(this, this::getUserInformation);
        getAvailableTest();
        adapter.startListening();
    }

    private void getAvailableTest() {
        testReference.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ParkinsonTest parkinsonTest = document.toObject(ParkinsonTest.class);
                            parkinsonTest.setId(document.getId());
                            testListAll.add(parkinsonTest);
                        }
                    } else {
                        createToast("Błąd podczas ładowania bazy danych");
                    }
                });
    }

    private void getUserInformation(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Toast.makeText(this, "Error while loading!", Toast.LENGTH_SHORT).show();
            Log.d("LOG_MAIN", e.toString());
            return;
        }
        if (documentSnapshot.exists()) {
            String email = documentSnapshot.getString(KEY_EMAIL);
            String[] split = email.split("@");
            String name = split[0].toUpperCase();
            etDoctorEmail.setText(name);
        }
    }


    private void onClickButton() {
        Intent intent = new Intent(this, CreateNewPatient.class);
        startActivity(intent);
    }

    private void setUpRecycleView() {
        Query query = patientReference.whereEqualTo("doctorID", auth.getUid());

        FirestoreRecyclerOptions<Patient> options = new FirestoreRecyclerOptions.Builder<Patient>()
                .setQuery(query, Patient.class)
                .build();

        adapter = new PatientListAdapter(options, getApplicationContext());

        RecyclerView recyclerView = findViewById(R.id.recycler_view_all_patient);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void init() {
        etDoctorEmail = (TextView) findViewById(R.id.doctor_name);
        btnNewPatient = (Button) findViewById(R.id.btnAddNewPatient);
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
    private void createToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
