package com.easybook.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.easybook.R;
import com.easybook.adapter.OrganizationAdapter;
import com.easybook.adapter.ScheduleAdapter;
import com.easybook.entity.Organization;
import com.easybook.entity.Schedule;
import com.easybook.util.RequestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ScheduleListFragment extends Fragment {
    public ScheduleListFragment() {
        super(R.layout.schedule_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = this.getActivity();

        String token = activity.
                getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", "");

        Request request = new Request.Builder()
                .url(RequestUtil.BASE_SCHEDULE_URL)
                .header("Authorization", token)
                .build();


        RequestUtil.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    Snackbar.make(view, response.message(), Snackbar.LENGTH_LONG).show();
                }
                try {
                    if (!response.isSuccessful()) {
                        Snackbar.make(view, response.message(), Snackbar.LENGTH_LONG).show();
                    }
                    RecyclerView recyclerView = view.findViewById(R.id.schedule_list);
                    String respStr = response.body().string();
                    List<Schedule> schedules =
                            RequestUtil.OBJECT_MAPPER.readValue(respStr,
                                    new TypeReference<List<Schedule>>() {
                                    });
                    ScheduleAdapter scheduleAdapter = new ScheduleAdapter(view.getContext(),
                            schedules,
                            getParentFragmentManager());
                    activity.runOnUiThread(() -> {
                        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                                DividerItemDecoration.VERTICAL));
                        recyclerView.setAdapter(scheduleAdapter);
                    });
                } catch (Exception e) {
                    Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}