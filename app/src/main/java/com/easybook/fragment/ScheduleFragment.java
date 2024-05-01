package com.easybook.fragment;

import android.app.Activity;
import android.content.Context;
import android.icu.util.LocaleData;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.easybook.R;
import com.easybook.adapter.DateSpinnerAdapter;
import com.easybook.adapter.SlotGridAdapter;
import com.easybook.entity.Schedule;
import com.easybook.entity.ScheduleDate;
import com.easybook.entity.Slot;
import com.easybook.util.RequestUtil;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ScheduleFragment extends Fragment implements
        AdapterView.OnItemSelectedListener{
    public ScheduleFragment() {
        super(R.layout.schedule);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = this.getActivity();

        ScheduleFragment thisFragment = this;

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
                    Spinner dateSpinner = view.findViewById(R.id.date_spinner);
                    dateSpinner.setOnItemSelectedListener(thisFragment);

                    GridView gridView = view.findViewById(R.id.gridview);
                    TextView scheduleTitle = view.findViewById(R.id.text_schedule_title);
                    String respStr = response.body().string();
                    Schedule schedule = RequestUtil.OBJECT_MAPPER.readValue(respStr, Schedule.class);

                    schedule.getAvailableDates().sort(Comparator.comparing(ScheduleDate::getDate));
                    schedule.getAvailableDates().forEach(date ->
                            date.getSlots().sort(Comparator.comparing(Slot::getStartTime)));
                    DateSpinnerAdapter dateAdapter = new DateSpinnerAdapter(view.getContext(),
                            android.R.layout.simple_spinner_item, schedule.getAvailableDates());
                    SlotGridAdapter slotGridAdapter = new SlotGridAdapter(view.getContext(),
                            android.R.layout.simple_list_item_1, schedule.getAvailableDates().get(0).getSlots());
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.date_spinner:
                Spinner spinner = getView().findViewById(R.id.date_spinner);
                GridView gridView = getView().findViewById(R.id.gridview);
                ScheduleDate scheduleDate = (ScheduleDate) parent.getItemAtPosition(position);
                SlotGridAdapter slotGridAdapter = new SlotGridAdapter(view.getContext(),
                        android.R.layout.simple_list_item_1, scheduleDate.getSlots());
                gridView.setAdapter(slotGridAdapter);
                break;
            case R.id.gridview:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
