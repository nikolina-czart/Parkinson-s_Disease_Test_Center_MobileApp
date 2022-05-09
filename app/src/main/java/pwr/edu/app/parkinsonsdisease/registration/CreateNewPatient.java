package pwr.edu.app.parkinsonsdisease.registration;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pwr.edu.app.parkinsonsdisease.AdminMainActivity;
import pwr.edu.app.parkinsonsdisease.DoctorMainActivity;
import pwr.edu.app.parkinsonsdisease.R;
import pwr.edu.app.parkinsonsdisease.adapter.ParkinsonTestAdapter;
import pwr.edu.app.parkinsonsdisease.adapter.ParkinsonTestSelectedListAdapter;
import pwr.edu.app.parkinsonsdisease.entity.ParkinsonTest;

public class CreateNewPatient extends AppCompatActivity {
    private FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    private FirebaseAuth fbAuthSecondary;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference testReference = db.collection("tests");

    private static final String LOG_DEBUG = "LOG_DEBUG";

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText passwordRepeatEditText;
    private Button addNewPatient;

    private ListView listView;
    final ArrayList<ParkinsonTest> testList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_patient);

        init();

        getAvailableTest();

        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyBcO_koY6g30W3WPj4maL5Qlkix85GqmZM")
                .setApplicationId("test-c1667").build();

        try {
            FirebaseApp myApp = FirebaseApp.initializeApp(getApplicationContext(), firebaseOptions, "AnyAppName");
            fbAuthSecondary = FirebaseAuth.getInstance(myApp);
        } catch (IllegalStateException e){
            fbAuthSecondary = FirebaseAuth.getInstance(FirebaseApp.getInstance("AnyAppName"));
        }

        addNewPatient.setOnClickListener(v -> onClickSignInButton());
    }

    private void getAvailableTest() {
        testReference.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ParkinsonTest parkinsonTest = document.toObject(ParkinsonTest.class);
                            parkinsonTest.setId(document.getId());
                            testList.add(parkinsonTest);
                        }
                        listView.setAdapter(new ParkinsonTestSelectedListAdapter(this, testList));
                    } else {
                        createToast("Błąd podczas ładowania bazy danych");
                    }
                });
    }


    private void init() {
        emailEditText = findViewById(R.id.etEmailNewPatient);
        passwordEditText = findViewById(R.id.etPasswordNewPatient);
        passwordRepeatEditText = findViewById(R.id.etPasswordRepeatNewPatient);
        addNewPatient = findViewById(R.id.btnCreateNewPatient);
        listView = (ListView)findViewById(R.id.listview);
    }

    private void onClickSignInButton() {

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String passwordRepeat = passwordRepeatEditText.getText().toString();

        Log.d(LOG_DEBUG, "Email: " + email + " " + "password: " + password);

        if (email.isEmpty()) {
            createToast("Proszę wpisać email");
        } else if (password.isEmpty()) {
            createToast("Proszę wpisać hasło");
        } else if (passwordRepeat.isEmpty()) {
            createToast("Proszę potwierdzić hasło");
        } else {
            if (password.equals(passwordRepeat)) {
                fbAuthSecondary.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                createDoctor(email);
                            } else {
                                createToast("Użytkownik zarejestowany | Problemy z połączeniem");
                            }
                        });
            } else {
                createToast("Hasła są różne");
            }
        }
    }

    private void createDoctor(String email) {
        ArrayList<ParkinsonTest> selectTestList = ((ParkinsonTestSelectedListAdapter)listView.getAdapter()).getSelectActorList();



        Map<String, Object> docPersonalData = new HashMap<>();
        docPersonalData.put("role", "PATIENT");
        docPersonalData.put("doctorID", fbAuth.getUid());
        docPersonalData.put("email", email);

        db.collection("users")
                .document(fbAuthSecondary.getUid())
                .set(docPersonalData)
                .addOnSuccessListener(aVoid -> {
                    createToast("Dodano poprawnie nowego pacjenta");
                    fbAuthSecondary.signOut();
                })
                .addOnFailureListener(e -> createToast("Błąd! Pacjent nie został dodany"));

        db.collection("users").whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            for(ParkinsonTest parkinsonTest : selectTestList){
                                if(parkinsonTest.isSelected()){
                                    Map<String, Object> docTestData = new HashMap<>();
                                    docTestData.put("name", parkinsonTest.getName());
                                    Map<String, Object> startData = new HashMap<>();
                                    db.collection("users").document(document.getId()).collection("tests").document(parkinsonTest.getId()).set(docTestData);
                                    db.collection("users").document(document.getId()).collection("testsHistory").document(parkinsonTest.getId()).set(docTestData);
                                    db.collection("users").document(document.getId()).collection("testsHistory").document(parkinsonTest.getId()).collection("testDates");
                                }
                            }
                        }
                        mainActivityRedirect();
                    } else {
                        createToast("Błąd! Testy pacjenta nie zapisane");
                    }
                });
    }

    private void mainActivityRedirect() {
        Intent myIntent = new Intent(this, DoctorMainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myIntent);
        finish();
    }

    private void createToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
