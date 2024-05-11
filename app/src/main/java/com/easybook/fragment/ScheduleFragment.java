package com.easybook.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.easybook.R;
import com.easybook.adapter.DateSpinnerAdapter;
import com.easybook.adapter.SlotGridAdapter;
import com.easybook.entity.Appointment;
import com.easybook.entity.Schedule;
import com.easybook.entity.ScheduleDate;
import com.easybook.entity.Slot;
import com.easybook.util.RequestUtil;

import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ScheduleFragment extends Fragment {

    public ScheduleFragment() {
        super(R.layout.schedule);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.organization_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = this.getActivity();

        String token = activity.
                getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", "");

        Request request = new Request.Builder()
                .url(RequestUtil.BASE_SCHEDULE_URL + "/" + getArguments().getString("id"))
                .header("Authorization", token)
                .build();


        RequestUtil.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                RequestUtil.makeSnackBar(activity, view, e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    RequestUtil.makeSnackBar(activity, view, response.message());
                }
                try {
                    if (!response.isSuccessful()) {
                        RequestUtil.makeSnackBar(activity, view, response.message());
                    }

                    TextView scheduleTitle = view.findViewById(R.id.text_schedule_title);
                    String respStr = response.body().string();
                    Schedule schedule = RequestUtil.OBJECT_MAPPER.readValue(respStr, Schedule.class);
                    GridView gridView = view.findViewById(R.id.gridview_slots);
                    Spinner dateSpinner = view.findViewById(R.id.date_spinner);
                    dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            GridView gridView = getView().findViewById(R.id.gridview_slots);
                            ScheduleDate scheduleDate = (ScheduleDate) parent.getItemAtPosition(position);
                            SlotGridAdapter slotGridAdapter = new SlotGridAdapter(view.getContext(),
                                    android.R.layout.simple_list_item_1, scheduleDate.getSlots(),
                                    schedule.getAppointments());
                            gridView.setAdapter(slotGridAdapter);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });

                    schedule.getAvailableDates().sort(Comparator.comparing(ScheduleDate::getDate));
                    schedule.getAvailableDates().forEach(date ->
                            date.getSlots().sort(Comparator.comparing(Slot::getStartTime)));
                    DateSpinnerAdapter dateAdapter = new DateSpinnerAdapter(view.getContext(),
                            android.R.layout.simple_spinner_item, schedule.getAvailableDates());
                    SlotGridAdapter slotGridAdapter = new SlotGridAdapter(view.getContext(),
                            android.R.layout.simple_selectable_list_item,
                            schedule.getAvailableDates().get(0).getSlots(),
                            schedule.getAppointments());
                    activity.runOnUiThread(() -> {
                        scheduleTitle.setText(schedule.getTitle());
                        dateSpinner.setAdapter(dateAdapter);
                        gridView.setAdapter(slotGridAdapter);
                    });
                } catch (Exception e) {
                    RequestUtil.makeSnackBar(activity, view, e.getMessage());
                }
            }
        });
    }
}
