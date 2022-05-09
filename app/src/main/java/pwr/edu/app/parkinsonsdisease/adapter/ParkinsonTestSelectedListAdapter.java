package pwr.edu.app.parkinsonsdisease.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import pwr.edu.app.parkinsonsdisease.R;
import java.util.ArrayList;
import android.widget.BaseAdapter;

import pwr.edu.app.parkinsonsdisease.entity.ParkinsonTest;
public class ParkinsonTestSelectedListAdapter extends BaseAdapter {
    ArrayList<ParkinsonTest> parkinsonTestArrayList;
    LayoutInflater vi;
    Context context;

    public ParkinsonTestSelectedListAdapter(Context context, ArrayList<ParkinsonTest> objects) {
        this.context = context;
        this.vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.parkinsonTestArrayList = objects;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // convert view = design
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = vi.inflate(R.layout.item_parkinson_test_selected, null);

            holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox_tests);

            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.tvName.setText(parkinsonTestArrayList.get(position).getName());
        holder.checkBox.setChecked(parkinsonTestArrayList.get(position).isSelected());

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSelected = ((CheckBox) v).isChecked();
                parkinsonTestArrayList.get(position).setSelected(isSelected);
            }
        });

        return convertView;

    }

    static class ViewHolder {
        public TextView tvName;
        public CheckBox checkBox;
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return parkinsonTestArrayList.size();
    }


    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return parkinsonTestArrayList.get(position);
    }


    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public ArrayList<ParkinsonTest> getSelectActorList() {
        ArrayList<ParkinsonTest> list = new ArrayList<>();
        for (int i = 0; i < parkinsonTestArrayList.size(); i++) {
            if (parkinsonTestArrayList.get(i).isSelected())
                list.add(parkinsonTestArrayList.get(i));
        }
        return list;
    }
}