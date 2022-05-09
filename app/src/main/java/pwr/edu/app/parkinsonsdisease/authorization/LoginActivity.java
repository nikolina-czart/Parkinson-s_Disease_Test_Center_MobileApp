package pwr.edu.app.parkinsonsdisease.authorization;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import pwr.edu.app.parkinsonsdisease.AdminMainActivity;
import pwr.edu.app.parkinsonsdisease.DoctorMainActivity;
import pwr.edu.app.parkinsonsdisease.PatientMainActivity;
import pwr.edu.app.parkinsonsdisease.R;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String LOG_DEBUG = "LOG_DEBUG";

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkCurrentUser();
        init();

        signInButton.setOnClickListener(v -> onClickSignInButton());
        }

    private void checkCurrentUser() {
        if(fbAuth.getCurrentUser() != null){
//            setFlags();
            redirectLoggedUser(fbAuth.getCurrentUser().getUid());
        }
    }

    private void init(){
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInButton = findViewById(R.id.signInButton);
    }

    private void onClickSignInButton() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        Log.d(LOG_DEBUG, "Email: " + email + " " + "password: " + password);

        if(email.isEmpty()){
            createToast("Proszę wpisać email");
        }else if(password.isEmpty()){
            createToast("Proszę wpisać hasło");
        }else {
            fbAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = fbAuth.getCurrentUser();
                            redirectLoggedUser(user.getUid());
                        } else {
                            createToast("Niepoprawne dane logowania");
                        }
                    });
        }
    }

    private void redirectLoggedUser(String uid) {
        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    if (Objects.requireNonNull(document.getString("role")).equals("PATIENT")) {
                        patientMainRedirect();
                    }
                    if (Objects.requireNonNull(document.getString("role")).equals("DOCTOR")) {
                        doctorMainRedirect();
                    }
                    if (Objects.requireNonNull(document.getString("role")).equals("ADMIN")) {
                        adminMainRedirect();
                    }
                    setContentView(R.layout.activity_main_patient);
                }
            }
        });
    }

    public void patientMainRedirect() {
        Intent myIntent = new Intent(this, PatientMainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);;
        startActivity(myIntent);
        finish();
    }

    public void doctorMainRedirect() {
        Intent myIntent = new Intent(this, DoctorMainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);;
        startActivity(myIntent);
        finish();
    }

    public void adminMainRedirect() {
        Intent myIntent = new Intent(this, AdminMainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);;
        startActivity(myIntent);
        finish();
    }

    private void setFlags(){
        Intent intent = new Intent(getApplicationContext(), PatientMainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void createToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}