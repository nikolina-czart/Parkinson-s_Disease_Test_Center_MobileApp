package pwr.edu.app.parkinsonsdisease.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import pwr.edu.app.parkinsonsdisease.R;
import pwr.edu.app.parkinsonsdisease.entity.ParkinsonTest;

public class ParkinsonTestAdapter extends FirestoreRecyclerAdapter<ParkinsonTest, ParkinsonTestAdapter.ParkinsonTestHolder> {
    private Context context;
    private OnItemClickListener listener;

    public ParkinsonTestAdapter(@NonNull FirestoreRecyclerOptions<ParkinsonTest> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ParkinsonTestHolder holder, int position, @NonNull ParkinsonTest model) {
        holder.nameParkinsonTest.setText(model.getName());
        int imageCode = context.getResources().getIdentifier(model.getImageName(), "drawable", context.getPackageName());
        if(imageCode == 0){
            imageCode = context.getResources().getIdentifier("broken_image", "drawable", context.getPackageName());
        }
        holder.testImageView.setImageResource(imageCode);
    }
    @NonNull
    @Override
    public ParkinsonTestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_test, parent, false);
        return new ParkinsonTestHolder(v);
    }
    class ParkinsonTestHolder extends RecyclerView.ViewHolder {
        TextView nameParkinsonTest;
        ImageView testImageView;

        public ParkinsonTestHolder(View itemView) {
            super(itemView);
            nameParkinsonTest = itemView.findViewById(R.id.nameParkinsonTest);
            testImageView = itemView.findViewById(R.id.testImageView);

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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}
