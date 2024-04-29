package com.easybook.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.easybook.R;
import com.easybook.entity.Slot;

import java.util.List;

public class SlotGridAdapter extends ArrayAdapter<Slot> {
    private final LayoutInflater inflater;

    private final int layout;
    private final List<Slot> slots;
    private final Context context;

    public SlotGridAdapter(@NonNull Context context, int resource, List<Slot> slots) {
        super(context, resource, slots);
        this.inflater = LayoutInflater.from(context);
        this.layout = resource;
        this.slots = slots;
        this.context = context;
    }

    @Override
    public Slot getItem(int position) {
        return slots.get(position);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView) convertView;

        if (convertView == null) {
            convertView = new TextView(context);
            label = (TextView) convertView;
        }
        label.setTextSize(20);
        label.setText(slots.get(position).getStartTime().toString());
        return convertView;
    }

}
