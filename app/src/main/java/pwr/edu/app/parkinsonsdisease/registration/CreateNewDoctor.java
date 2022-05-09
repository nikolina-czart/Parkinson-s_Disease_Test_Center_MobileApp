package pwr.edu.app.parkinsonsdisease.registration;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import pwr.edu.app.parkinsonsdisease.AdminMainActivity;
import pwr.edu.app.parkinsonsdisease.DoctorMainActivity;
import pwr.edu.app.parkinsonsdisease.PatientMainActivity;
import pwr.edu.app.parkinsonsdisease.R;

public class CreateNewDoctor extends AppCompatActivity {
    private FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth fbAuthSecondary;

    private static final String LOG_DEBUG = "LOG_DEBUG";

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText passwordRepeatEditText;
    private Button addNewDoctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_doctor);

        init();

        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyBcO_koY6g30W3WPj4maL5Qlkix85GqmZM")
                .setApplicationId("test-c1667").build();

        try {
            FirebaseApp myApp = FirebaseApp.initializeApp(getApplicationContext(), firebaseOptions, "AnyAppName");
            fbAuthSecondary = FirebaseAuth.getInstance(myApp);
        } catch (IllegalStateException e){
            fbAuthSecondary = FirebaseAuth.getInstance(FirebaseApp.getInstance("AnyAppName"));
        }

        addNewDoctor.setOnClickListener(v -> onClickSignInButton());
    }


    private void init(){
        emailEditText = findViewById(R.id.etEmailNewDoctor);
        passwordEditText = findViewById(R.id.etPasswordNewDoctor);
        passwordRepeatEditText = findViewById(R.id.etPasswordRepeatNewDoctor);
        addNewDoctor = findViewById(R.id.btnCreateNewDoctor);
    }

    private void onClickSignInButton() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String passwordRepeat = passwordRepeatEditText.getText().toString();

        Log.d(LOG_DEBUG, "Email: " + email + " " + "password: " + password);

        if(email.isEmpty()){
            createToast("Proszę wpisać email");
        }else if(password.isEmpty()) {
            createToast("Proszę wpisać hasło");
        }else if(passwordRepeat.isEmpty()) {
            createToast("Proszę potwierdzić hasło");
        }else {
            if(password.equals(passwordRepeat)){
                fbAuthSecondary.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                createDoctor();
                            } else {
                                createToast("Użytkownik zarejestowany | Problemy z połączeniem");
                            }
                        });
            }else{
                createToast("Hasła są różne");
            }
        }
    }

    private void createDoctor() {
        Map<String, Object> docPersonalData = new HashMap<>();
        docPersonalData.put("role", "DOCTOR");
        docPersonalData.put("doctorEmail", fbAuthSecondary.getCurrentUser().getEmail());

        db.collection("users").document(fbAuthSecondary.getCurrentUser().getUid())
                .set(docPersonalData)
                .addOnSuccessListener(aVoid -> {
                    createToast("Dodano poprawnie nowego lekarza");
                    mainActivityRedirect();
                })
                .addOnFailureListener(e -> createToast("Błąd! Lekarz nie został dodany"));
    }

    private void mainActivityRedirect() {
        Intent myIntent = new Intent(this, AdminMainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myIntent);
        finish();
    }

    private void createToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}