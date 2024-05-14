package com.easybook.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
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
import com.easybook.adapter.AppointmentAdapter;
import com.easybook.entity.Appointment;
import com.easybook.entity.Organization;
import com.easybook.util.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class AppointmentListFragment extends Fragment {

    private String token;

    private AppointmentAdapter appointmentAdapter;

    public AppointmentListFragment() {
        super(R.layout.list);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.appointment_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        Activity activity = this.getActivity();

        token = activity.
                getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", "");

        Request request = new Request.Builder()
                .url(RequestUtil.BASE_APPOINTMENT_URL)
                .header("Authorization", token)
                .build();


        RequestUtil.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                RequestUtil.makeSnackBar(activity, view, e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (!response.isSuccessful()) {
                        RequestUtil.makeSnackBar(activity, view, response.message());
                    }
                    RecyclerView recyclerView = view.findViewById(R.id.list);
                    String respStr = response.body().string();
                    List<Appointment> appointments =
                            RequestUtil.OBJECT_MAPPER.readValue(respStr,
                                    new TypeReference<List<Appointment>>() {
                                    });
                    appointmentAdapter = new AppointmentAdapter(view.getContext(),
                            appointments,
                            getParentFragmentManager());
                    activity.runOnUiThread(() -> {
                        if (appointments.size() == 0) {
                            TextView notExistingView = view.findViewById(R.id.not_existing_message);
                            if (getArguments() == null) {
                                notExistingView.setText("У вас пока нет записей");
                            } else {
                                notExistingView.setText(getArguments().getString("message"));
                            }
                            notExistingView.setVisibility(View.VISIBLE);
                        }
                        recyclerView.setAdapter(appointmentAdapter);
                        registerForContextMenu(recyclerView);
                    });
                } catch (Exception e) {
                    RequestUtil.makeSnackBar(activity, view, e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.appointment_list_menu_create_appointment: {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle("Создание записи");
                dialog.setMessage("Введите идентификатор расписания или организации");

                LayoutInflater inflater = LayoutInflater.from(getContext());
                View createOrganizationWindow = inflater.inflate(R.layout.create_appointment, null);
                dialog.setView(createOrganizationWindow);

                final MaterialEditText id = createOrganizationWindow.findViewById(R.id.field_enter_organization_or_schedule_id);

                dialog.setNegativeButton("Назад", (dialogInterface, i) -> dialogInterface.dismiss());

                dialog.setPositiveButton("Перейти", (dialogInterface, i) -> {
                    idIsOrganization(id.getText().toString());
                });
                dialog.show();
                break;
            }
        }
        return false;
    }

    private void idIsOrganization(String id) {
        Request request = new Request.Builder()
                .url(RequestUtil.BASE_ORGANIZATION_URL + "/" + id)
                .header("Authorization", token)
                .build();
        RequestUtil.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                RequestUtil.makeSnackBar(getActivity(), getView(), e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    try {
                        getActivity().runOnUiThread(() -> {
                            OrganizationFragment organizationFragment = new OrganizationFragment();
                            Bundle arguments = new Bundle();
                            arguments.putString("id", id);
                            organizationFragment.setArguments(arguments);
                            getParentFragmentManager().beginTransaction().replace(R.id.fragment_container_view,
                                    organizationFragment, "ORGANIZATION").commit();
                        });
                    } catch (Exception e) {
                        RequestUtil.makeSnackBar(getActivity(), getView(), e.getMessage());
                    }
                } else {
                    idIsSchedule(id);
                }
            }
        });
    }

    private void idIsSchedule(String id) {
        Request request = new Request.Builder()
                .url(RequestUtil.BASE_SCHEDULE_URL + "/" + id)
                .header("Authorization", token)
                .build();
        RequestUtil.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                RequestUtil.makeSnackBar(getActivity(), getView(), e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    try {
                        getActivity().runOnUiThread(() -> {
                            ScheduleFragment scheduleFragment = new ScheduleFragment();
                            Bundle arguments = new Bundle();
                            arguments.putString("id", id);
                            scheduleFragment.setArguments(arguments);
                            getParentFragmentManager().beginTransaction().replace(R.id.fragment_container_view,
                                    scheduleFragment, "SCHEDULE").commit();
                        });
                    } catch (Exception e) {
                        RequestUtil.makeSnackBar(getActivity(), getView(), e.getMessage());
                    }
                } else {
                    RequestUtil.makeSnackBar(getActivity(), getView(), "Некорректный идентификатор");
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
                dialog.setTitle("Вы уверены что хотите удалить запись?");
                dialog.setNegativeButton("Нет", (dialogInterface, i) -> dialogInterface.dismiss());
                dialog.setPositiveButton("Да", (dialogInterface, i) -> {
                    UUID id = appointmentAdapter.deleteAppointment();
                    deleteAppointment(id);
                });
                dialog.show();
        }
        return super.onContextItemSelected(item);
    }

    public void deleteAppointment(UUID id) {
        Request request = new Request.Builder()
                .url(RequestUtil.BASE_APPOINTMENT_URL + "/" + id)
                .delete()
                .header("Authorization", token)
                .build();
        RequestUtil.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                RequestUtil.makeSnackBar(getActivity(), getView(), e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    RequestUtil.makeSnackBar(getActivity(), getView(), response.message());
                }
                getActivity().runOnUiThread(() -> {
                    getParentFragmentManager().beginTransaction().replace(R.id.fragment_container_view,
                            AppointmentListFragment.class, null).commit();
                });
            }
        });
    }
}
