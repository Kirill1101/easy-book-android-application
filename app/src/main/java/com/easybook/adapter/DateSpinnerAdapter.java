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
import com.easybook.util.RequestUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DateSpinnerAdapter extends ArrayAdapter<ScheduleDate> {
    private final LayoutInflater inflater;

    private final int layout;
    private final List<ScheduleDate> dates;
    private final Context context;
    private Integer currentPosition;

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

        currentPosition = position;

        label.setTextSize(20);
        label.setText(dates.get(position).getDate().toString());
        return convertView;
    }

    public ScheduleDate getCurrentItem() {
        return dates.get(currentPosition);
    }

    public UUID deleteDate() {
        if (currentPosition != null) {
            boolean occupiedSlotIsExist = dates.stream()
                    .flatMap(dates -> dates.getSlots().stream())
                    .anyMatch(slot -> slot.getAppointmentId() != null);
            if (!occupiedSlotIsExist) {
                UUID id = dates.get(currentPosition).getId();
                dates.remove((int) currentPosition);
                currentPosition = null;
                return id;
            }
        }
        return null;
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
