package com.easybook.fragment;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.easybook.R;
import com.easybook.adapter.DateSpinnerAdapter;
import com.easybook.adapter.SlotGridAdapter;
import com.easybook.entity.Appointment;
import com.easybook.entity.Schedule;
import com.easybook.entity.ScheduleDate;
import com.easybook.entity.Service;
import com.easybook.entity.Slot;
import com.easybook.util.RequestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ScheduleFragment extends Fragment {

    private TextView scheduleTitle;
    private GridView gridView;
    private Spinner dateSpinner;
    private Schedule schedule;
    private String token, login;

    private boolean currentUserIsCreator;
    private Appointment currentUserAppointment;

    public ScheduleFragment() {
        super(R.layout.schedule);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.schedule_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        Activity activity = this.getActivity();

        token = activity.
                getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", "");
        login = activity.
                getSharedPreferences("auth", Context.MODE_PRIVATE).getString("login", "");

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
                try {
                    if (!response.isSuccessful()) {
                        RequestUtil.makeSnackBar(activity, view, response.message());
                    }
                    String respStr = response.body().string();
                    schedule = RequestUtil.OBJECT_MAPPER.readValue(respStr, Schedule.class);
                    if (schedule.getAvailableDates().size() == 0) {
                        RequestUtil.makeSnackBar(activity, view, "В раписании нет подходящих слотов для записи");
                    }
                    currentUserIsCreator = schedule.getUserCreatorLogin().equals(login);
                    scheduleTitle = view.findViewById(R.id.schedule_title);
                    gridView = view.findViewById(R.id.gridview_slots);
                    dateSpinner = view.findViewById(R.id.date_spinner);
                    dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            GridView gridView = getView().findViewById(R.id.gridview_slots);
                            ScheduleDate scheduleDate = (ScheduleDate) parent.getItemAtPosition(position);
                            if (currentUserAppointment != null) {
                                currentUserAppointment.setDate(scheduleDate.getDate());
                            }
                            SlotGridAdapter slotGridAdapter = new SlotGridAdapter(view.getContext(),
                                    android.R.layout.simple_list_item_1, scheduleDate.getSlots(),
                                    schedule.getAppointments(), getActivity(), getView(),
                                    getParentFragmentManager(), token, currentUserAppointment);
                            gridView.setAdapter(slotGridAdapter);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });
                    if (currentUserIsCreator) {
                        setAdapters(schedule.getAvailableDates());
                    } else {
                        currentUserAppointment = new Appointment();
                        currentUserAppointment.setScheduleTitle(schedule.getTitle());
                        if (schedule.getServices().size() != 0) {
                            dialogSelectServices();
                        } else {
                            currentUserAppointment.setDuration(schedule.getDurationOfOneSlot());
                            getAvailableDatesForSpecifiedDurationRequest();
                        }
                    }
                } catch (Exception e) {
                    RequestUtil.makeSnackBar(activity, view, e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.schedule_menu_service_editor: {
                ServiceListFragment serviceListFragment = new ServiceListFragment();
                Bundle arguments = new Bundle();
                arguments.putString("scheduleId", schedule.getId().toString());
                serviceListFragment.setArguments(arguments);
                getActivity().runOnUiThread(() -> {
                    getParentFragmentManager().beginTransaction().replace(R.id.fragment_container_view,
                            serviceListFragment, "SERVICE_LIST").commit();
                });
                break;
            }
            case R.id.schedule_menu_edit_schedule:
                break;
            case R.id.schedule_menu_share:
                dialogShare();
                break;
        }
        return false;
    }

    private void dialogShare() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Идентификатор расписания");
        dialog.setMessage("Поделитесь с теми, кому хотите дать доступ для записи");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View shareIdWindow = inflater.inflate(R.layout.share_id_window, null);
        dialog.setView(shareIdWindow);
        final MaterialEditText scheduleId = shareIdWindow.findViewById(R.id.text_share_id);
        scheduleId.setText(schedule.getId().toString());

        dialog.setNegativeButton("Назад", (dialogInterface, i) -> dialogInterface.dismiss());

        dialog.setPositiveButton("Копировать", (dialogInterface, i) -> {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", schedule.getId().toString());
            clipboard.setPrimaryClip(clip);
        });

        dialog.show();
    }

    private void dialogSelectServices() {
        AlertDialog.Builder selectServiceDialog = new AlertDialog.Builder(getContext());
        selectServiceDialog.setCancelable(false);
        selectServiceDialog.setTitle("Выберите интересующие вас услуги");

        boolean[] selectedService = new boolean[schedule.getServices().size()];
        List<Integer> serviceListInd = new ArrayList<>();
        String[] services = new String[schedule.getServices().size()];
        for (int i = 0; i < schedule.getServices().size(); i++) {
            Service service = schedule.getServices().get(i);
            services[i] = service.getTitle() + " \n" + service.getPrice() + " \n" + service.getDuration();
        }
        selectServiceDialog.setMultiChoiceItems(services, selectedService, (dialogInterface, i, b) -> {
            if (b) {
                serviceListInd.add(i);
            } else {
                serviceListInd.remove(Integer.valueOf(i));
            }
        });

        selectServiceDialog.setPositiveButton("Ok", (dialogInterface, i) -> {
            StringBuilder stringBuilder = new StringBuilder();
            List<Service> serviceList = new ArrayList<>();
            for (int j = 0; j < serviceListInd.size(); j++) {
                serviceList.add(schedule.getServices().get(serviceListInd.get(j)));
            }
            currentUserAppointment.setServices(serviceList);
            currentUserAppointment.setDuration(schedule.getServices().stream().mapToLong(Service::getDuration).sum());

            getAvailableDatesForSpecifiedDurationRequest();
        });

        selectServiceDialog.setNegativeButton("Назад", (dialogInterface, i) -> dialogInterface.dismiss());
        selectServiceDialog.show();
    }

    private void getAvailableDatesForSpecifiedDurationRequest() {
        Request request = new Request.Builder()
                .url(RequestUtil.BASE_SCHEDULE_URL + "/" + getArguments().getString("id")
                        + "?durationInSeconds=" + currentUserAppointment.getDuration())
                .header("Authorization", token)
                .build();

        RequestUtil.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                RequestUtil.makeSnackBar(getActivity(), getView(), e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (!response.isSuccessful()) {
                        RequestUtil.makeSnackBar(getActivity(), getView(), response.message());
                    }
                    String respStr = response.body().string();
                    List<ScheduleDate> scheduleDate = RequestUtil.OBJECT_MAPPER.readValue(respStr,
                            new TypeReference<List<ScheduleDate>>() {
                            });
                    setAdapters(scheduleDate);

                } catch (Exception e) {
                    RequestUtil.makeSnackBar(getActivity(), getView(), e.getMessage());
                }
            }
        });
    }

    private void setAdapters(List<ScheduleDate> scheduleDates) {
        scheduleDates.sort(Comparator.comparing(ScheduleDate::getDate));
        scheduleDates.forEach(date ->
                date.getSlots().sort(Comparator.comparing(Slot::getStartTime)));
        DateSpinnerAdapter dateAdapter = new DateSpinnerAdapter(getContext(),
                android.R.layout.simple_spinner_item, scheduleDates);
        SlotGridAdapter slotGridAdapter = new SlotGridAdapter(getView().getContext(),
                android.R.layout.simple_list_item_1, scheduleDates.get(0).getSlots(),
                schedule.getAppointments(), getActivity(), getView(),
                getParentFragmentManager(), token, currentUserAppointment);
        if (currentUserAppointment != null) {
            currentUserAppointment.setDate(scheduleDates.get(0).getDate());
        }
        getActivity().runOnUiThread(() -> {
            scheduleTitle.setText(schedule.getTitle());
            dateSpinner.setAdapter(dateAdapter);
            gridView.setAdapter(slotGridAdapter);
        });
    }
}
