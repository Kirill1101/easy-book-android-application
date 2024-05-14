package com.easybook.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.easybook.R;
import com.easybook.entity.Appointment;
import com.easybook.entity.Slot;
import com.easybook.fragment.AppointmentListFragment;
import com.easybook.util.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SlotGridAdapter extends ArrayAdapter<Slot> {
    private final LayoutInflater inflater;

    private final int layout;
    private final Context context;
    private final Activity activity;
    private final View view;
    private final FragmentManager fragmentManager;
    private List<Slot> slots;
    private List<Appointment> appointments;
    private Appointment clientAppointment;
    private String token;

    public SlotGridAdapter(@NonNull Context context, int resource, List<Slot> slots,
                           List<Appointment> appointments, Activity activity, View view,
                           FragmentManager fragmentManager, String token, Appointment clientAppointment) {
        super(context, resource, slots);

        this.inflater = LayoutInflater.from(context);
        this.layout = resource;
        this.slots = slots;
        this.context = context;
        this.appointments = appointments;
        this.activity = activity;
        this.view = view;
        this.fragmentManager = fragmentManager;
        this.token = token;
        this.clientAppointment = clientAppointment;
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
        label.setBackgroundResource(R.drawable.rounded_square_shape);
        label.setTextSize(20);
        label.setPadding(5, 5, 5, 5);
        label.setGravity(Gravity.CENTER);
        label.setText(slots.get(position).getStartTime().toString());

        Slot slot = slots.get(position);

        if (clientAppointment == null) {
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
        } else {
            convertView.setOnClickListener(view -> {
                LocalTime endTime = slot.getStartTime().plusSeconds(clientAppointment.getDuration());
                clientAppointment.setStartTime(slot.getStartTime());
                clientAppointment.setEndTime(endTime);
                AlertDialog.Builder dialog = new AlertDialog.Builder(this.getContext());
                dialog.setMessage("Вы уверены, что хотите записаться на время [" +
                        slot.getStartTime() + " - " + endTime + "]");
                dialog.setNegativeButton("Нет", (dialogInterface, i) -> dialogInterface.dismiss());
                dialog.setPositiveButton("Да", (dialogInterface, i) -> {
                    createAppointmentRequest();
                });
                dialog.show();
                dialog.show();
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

    private void createAppointmentRequest() {
        RequestBody body = null;
        try {
            body = RequestBody.create(
                    RequestUtil.OBJECT_MAPPER.writeValueAsString(clientAppointment), RequestUtil.MEDIA_TYPE);
        } catch (JsonProcessingException e) {
            RequestUtil.makeSnackBar(activity, view, e.getMessage());
            return;
        }

        Request request = new Request.Builder()
                .url(RequestUtil.BASE_APPOINTMENT_URL)
                .post(body)
                .header("Authorization", token)
                .build();
        RequestUtil.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                RequestUtil.makeSnackBar(activity, view, e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    try {
                        activity.runOnUiThread(() -> {
                            fragmentManager.beginTransaction().replace(R.id.fragment_container_view,
                                    AppointmentListFragment.class, null).commit();
                            RequestUtil.makeSnackBar(activity, view, "Запись успешно создана");
                        });
                    } catch (Exception e) {
                        RequestUtil.makeSnackBar(activity, view, e.getMessage());
                    }
                }
            }
        });
    }
}
