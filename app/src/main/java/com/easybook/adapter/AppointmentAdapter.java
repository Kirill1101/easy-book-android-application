package com.easybook.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.easybook.R;
import com.easybook.entity.Appointment;
import com.easybook.util.Util;

import java.util.List;
import java.util.UUID;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
    private final LayoutInflater inflater;

    private final List<Appointment> appointments;

    private final FragmentManager fragmentManager;

    private final Context context;

    private int contextPosition;

    public AppointmentAdapter(Context context, List<Appointment> appointments,
                              FragmentManager fragmentManager) {
        this.context = context;
        this.appointments = appointments;
        this.inflater = LayoutInflater.from(context);
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public AppointmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.appointment_list_item, parent, false);
        return new AppointmentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentAdapter.ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        String timeStr = appointment.getStartTime().toString()
                + " - " + appointment.getEndTime().toString();

        holder.title.setText(appointment.getScheduleTitle());
        holder.date.setText(appointment.getDate().toString());
        holder.time.setText(timeStr);

        holder.itemView.setOnClickListener((view) -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(Util.getAppointmentStringByAppointment(appointment));
            dialog.show();
        });

        holder.itemView.setOnLongClickListener((view) -> {
            contextPosition = position;
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public UUID deleteAppointment() {
        UUID id = appointments.get(contextPosition).getId();
        appointments.remove(contextPosition);
        return id;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title, date, time;

        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.appointment_item_schedule_title);
            date = view.findViewById(R.id.appointment_item_date);
            time = view.findViewById(R.id.appointment_item_time);
        }
    }
}
