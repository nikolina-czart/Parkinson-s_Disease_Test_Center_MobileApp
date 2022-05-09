package pwr.edu.app.parkinsonsdisease.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import pwr.edu.app.parkinsonsdisease.R;
import pwr.edu.app.parkinsonsdisease.entity.Doctor;
import pwr.edu.app.parkinsonsdisease.entity.Patient;

public class PatientListAdapter extends FirestoreRecyclerAdapter<Patient, PatientListAdapter.PatientListHolder> {
    private Context context;
    private ParkinsonTestAdapter.OnItemClickListener listener;

    public PatientListAdapter(@NonNull FirestoreRecyclerOptions<Patient> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull PatientListHolder holder, int position, @NonNull Patient model) {
        holder.emailPatient.setText(model.getEmail());
    }

    @NonNull
    @Override
    public PatientListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_patient, parent, false);
        return new PatientListHolder(v);
    }

    class PatientListHolder extends RecyclerView.ViewHolder {
        TextView emailPatient;

        public PatientListHolder(View itemView) {
            super(itemView);
            emailPatient = itemView.findViewById(R.id.patientEmail);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(ParkinsonTestAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

}