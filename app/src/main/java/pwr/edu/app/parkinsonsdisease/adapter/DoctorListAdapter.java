package pwr.edu.app.parkinsonsdisease.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import pwr.edu.app.parkinsonsdisease.R;
import pwr.edu.app.parkinsonsdisease.entity.Doctor;
import pwr.edu.app.parkinsonsdisease.entity.ParkinsonTest;

public class DoctorListAdapter extends FirestoreRecyclerAdapter<Doctor, DoctorListAdapter.DoctorListHolder> {
    private Context context;

    public DoctorListAdapter(@NonNull FirestoreRecyclerOptions<Doctor> options, Context context) {
        super(options);
        this.context = context;
    }


    @Override
    protected void onBindViewHolder(@NonNull DoctorListHolder holder, int position, @NonNull Doctor model) {
        holder.emailDoctor.setText(model.getDoctorEmail());
    }

    @NonNull
    @Override
    public DoctorListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_doctor, parent, false);
        return new DoctorListHolder(v);
    }
    class DoctorListHolder extends RecyclerView.ViewHolder {
        TextView emailDoctor;

        public DoctorListHolder(View itemView) {
            super(itemView);
            emailDoctor = itemView.findViewById(R.id.emailDoctor);
        }
    }
}
