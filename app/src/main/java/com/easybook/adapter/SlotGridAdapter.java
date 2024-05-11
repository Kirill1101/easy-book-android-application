package com.easybook.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.easybook.entity.Appointment;
import com.easybook.entity.Slot;

import java.util.List;
import java.util.Optional;

public class SlotGridAdapter extends ArrayAdapter<Slot> {
    private final LayoutInflater inflater;

    private final int layout;
    private final Context context;
    private List<Slot> slots;
    private List<Appointment> appointments;

    public SlotGridAdapter(@NonNull Context context, int resource,
                           List<Slot> slots, List<Appointment> appointments) {
        super(context, resource, slots);

        this.inflater = LayoutInflater.from(context);
        this.layout = resource;
        this.slots = slots;
        this.context = context;
        this.appointments = appointments;
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

        Slot slot = slots.get(position);
        if (slot.getAppointmentId() != null) {
            label.setBackgroundColor(Color.rgb(190, 190, 190));

            convertView.setOnClickListener(view -> {
                Optional<Appointment> appointmentOptional = appointments.stream()
                        .filter(a -> a.getId().equals(slot.getAppointmentId()))
                        .findFirst();
                if (appointmentOptional.isPresent()) {
                    Appointment appointment = appointmentOptional.get();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this.getContext());
                    dialog.setMessage(getAppointmentStringByAppointment(appointment));
                    dialog.show();
                }
            });
        }

        return convertView;
    }

    private String getAppointmentStringByAppointment(Appointment appointment) {
        StringBuilder appointmentInfo = new StringBuilder();
        appointmentInfo
                .append("Пользователь: ")
                .append(appointment.getUserLogin())
                .append("\nВремя записи: ")
                .append(appointment.getStartTime())
                .append(" - ")
                .append(appointment.getEndTime())
                .append("\nДлительность записи: ")
                .append(getDurationStringBySeconds(appointment.getDuration()))
                .append("\nУслуги:");
        appointment.getServices().forEach(service ->
                appointmentInfo
                        .append("\n \t \u2022 ")
                        .append(service.getTitle())
                        .append(" (")
                        .append(getDurationStringBySeconds(service.getDuration()))
                        .append(")"));
        return appointmentInfo.toString();
    }

    private String getDurationStringBySeconds(Long duration) {
        StringBuilder durationStr = new StringBuilder();
        if (duration / 3600 > 0) {
            durationStr.append(duration / 3600).append("ч ");
        }
        if ((duration % 3600) / 60 > 0) {
            durationStr.append((duration % 3600) / 60).append("м");
        }
        return durationStr.toString();
    }
}
