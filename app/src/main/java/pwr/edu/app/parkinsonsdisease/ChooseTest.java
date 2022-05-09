package pwr.edu.app.parkinsonsdisease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import pwr.edu.app.parkinsonsdisease.adapter.ParkinsonTestAdapter;
import pwr.edu.app.parkinsonsdisease.entity.ParkinsonTest;

public class ChooseTest extends AppCompatActivity {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference testReference = db.collection("users").document(auth.getUid()).collection("tests");

    private ParkinsonTestAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_test);

        setUpRecycleView();
    }

    private void setUpRecycleView() {
        Query query = testReference;

        FirestoreRecyclerOptions<ParkinsonTest> options = new FirestoreRecyclerOptions.Builder<ParkinsonTest>()
                .setQuery(query, ParkinsonTest.class)
                .build();

        adapter = new ParkinsonTestAdapter(options, getApplicationContext());

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener((documentSnapshot, position) -> {
            ParkinsonTest parkinsonTest = documentSnapshot.toObject(ParkinsonTest.class);

            try {
                String activityToStart =
                        "pwr.edu.app.parkinsonsdisease.testimplementation."
                        + parkinsonTest.getPackageName() + "." + parkinsonTest.getClassName();
                Class<?> c = Class.forName(activityToStart);
                Intent intent = new Intent(this, c);
                startActivity(intent);
            } catch (ClassNotFoundException ignored) {
                Toast.makeText(ChooseTest.this, "Nie zaimplementowano testu",
                        Toast.LENGTH_SHORT).show();
            }
        });
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
}
