package com.easybook.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.easybook.entity.ScheduleDate;
import com.easybook.entity.Slot;

import java.util.List;

public class DateSpinnerAdapter extends ArrayAdapter<ScheduleDate> {
    private final LayoutInflater inflater;

    private final int layout;
    private final List<ScheduleDate> dates;
    private final Context context;

    public DateSpinnerAdapter(@NonNull Context context, int resource, List<ScheduleDate> dates) {
        super(context, resource, dates);
        this.inflater = LayoutInflater.from(context);
        this.layout = resource;
        this.dates = dates;
        this.context = context;
    }

    @Override
    public ScheduleDate getItem(int position) {
        return dates.get(position);
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
        label.setText(dates.get(position).getDate().toString());
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView) convertView;

        if (convertView == null) {
            convertView = new TextView(context);
            label = (TextView) convertView;
        }
        label.setTextSize(20);
        label.setText(dates.get(position).getDate().toString());
        return convertView;
    }
}
