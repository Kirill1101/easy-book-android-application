package com.easybook.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.easybook.R;
import com.easybook.adapter.ScheduleAdapter;
import com.easybook.entity.Schedule;
import com.easybook.util.RequestUtil;
import com.easybook.util.ScheduleRequestUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ScheduleListFragment extends Fragment {

    private ScheduleAdapter scheduleAdapter;

    private String token;

    private ScheduleRequestUtil scheduleRequestUtil;

    public ScheduleListFragment() {
        super(R.layout.list);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.schedule_list_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        Activity activity = this.getActivity();

        token = activity.
                getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", "");

        scheduleRequestUtil = new ScheduleRequestUtil(token, getActivity(),
                getContext(), getView(), getParentFragmentManager());

        Request request = new Request.Builder()
                .url(RequestUtil.BASE_SCHEDULE_URL)
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
                    RecyclerView recyclerView = view.findViewById(R.id.list);
                    String respStr = response.body().string();
                    List<Schedule> schedules =
                            RequestUtil.OBJECT_MAPPER.readValue(respStr,
                                    new TypeReference<List<Schedule>>() {
                                    });
                    scheduleAdapter = new ScheduleAdapter(view.getContext(),
                            schedules,
                            getParentFragmentManager());
                    activity.runOnUiThread(() -> {
                        if (schedules.size() == 0) {
                            TextView notExistingView = view.findViewById(R.id.not_existing_message);
                            notExistingView.setText("У вас пока нет расписаний");
                            notExistingView.setVisibility(View.VISIBLE);
                        }
                        registerForContextMenu(recyclerView);
                        recyclerView.setAdapter(scheduleAdapter);
                    });
                } catch (Exception e) {
                    RequestUtil.makeSnackBar(activity, view, e.getMessage());
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v,
                                    @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, 1, Menu.NONE, "Удалить");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle("Вы уверены что хотите удалить расписание?");
                dialog.setNegativeButton("Нет", (dialogInterface, i) -> dialogInterface.dismiss());
                dialog.setPositiveButton("Да", (dialogInterface, i) -> {
                    UUID id = scheduleAdapter.deleteSchedule();
                    scheduleRequestUtil.deleteScheduleRequest(id);
                });
                dialog.show();
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.schedule_list_menu_create_schedule: {
                scheduleRequestUtil.showCreateScheduleWindow();
                break;
            }
        }
        return false;
    }
}
