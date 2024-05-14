package com.easybook.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentManager;

import com.easybook.R;
import com.easybook.entity.Schedule;
import com.easybook.entity.ScheduleDate;
import com.easybook.entity.Slot;
import com.easybook.fragment.OrganizationFragment;
import com.easybook.fragment.ScheduleFragment;
import com.easybook.fragment.ScheduleListFragment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ScheduleRequestUtil {
    private String token;
    private Activity activity;
    private Context context;
    private View view;
    private FragmentManager fragmentManager;
    private UUID organizationId;
    private LocalDate pickedStartDate, pickedEndDate;
    private LocalTime pickedStartTime, pickedEndTime;
    private List<Integer> daysList;

    public ScheduleRequestUtil(String token, Activity activity, Context context, View view, FragmentManager fragmentManager) {
        this.token = token;
        this.activity = activity;
        this.context = context;
        this.view = view;
        this.fragmentManager = fragmentManager;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public void showAddDatesScheduleWindow(Schedule schedule) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);

        LayoutInflater inflater = LayoutInflater.from(context);
        View addDatesWindow = inflater.inflate(R.layout.create_schedule, null);
        dialog.setView(addDatesWindow);

        TextInputEditText checkboxFreeTimeText = addDatesWindow.findViewById(R.id.create_schedule_checkbox_free_time_text);

        addDatesWindow.findViewById(R.id.create_schedule_schedule_title_layout).setVisibility(View.GONE);
        addDatesWindow.findViewById(R.id.create_schedule_duration_layout).setVisibility(View.GONE);

        setDateTimePickerToField(addDatesWindow);

        dialog.setNegativeButton("Назад", (dialogInterface, i) -> dialogInterface.dismiss());

        dialog.setPositiveButton("Создать", (dialogInterface, i) -> {
            if (pickedStartDate == null || pickedEndDate == null) {
                RequestUtil.makeSnackBar(activity, view, "Выберите начало и конец рабочего дня");
                return;
            }
            if (pickedStartDate.toEpochDay() >= pickedEndDate.toEpochDay()) {
                RequestUtil.makeSnackBar(activity, view, "Начало рабочего времени не может быть позже чем его конец");
                return;
            }
            addScheduleDatesToSchedule(schedule, daysList, checkboxFreeTimeText.getText().toString());
        });
        dialog.show();
    }

    public void showCreateScheduleWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);

        LayoutInflater inflater = LayoutInflater.from(context);
        View createScheduleWindow = inflater.inflate(R.layout.create_schedule, null);
        dialog.setView(createScheduleWindow);

        TextInputEditText titleText = createScheduleWindow.findViewById(R.id.create_schedule_schedule_title_text);
        TextInputEditText checkboxFreeTimeText = createScheduleWindow.findViewById(R.id.create_schedule_checkbox_free_time_text);
        AutoCompleteTextView durationAutoCompleteTextView = createScheduleWindow.findViewById(R.id.create_schedule_duration_auto_complete);


        ArrayAdapter<String> adapter = new ArrayAdapter(activity,
                R.layout.list_item,
                new String[]{"5м", "10м", "15м", "20м", "30м", "45м", "60м", "1ч 30м"});
        durationAutoCompleteTextView.setAdapter(adapter);

        setDateTimePickerToField(createScheduleWindow);

        dialog.setNegativeButton("Назад", (dialogInterface, i) -> dialogInterface.dismiss());

        dialog.setPositiveButton("Создать", (dialogInterface, i) -> {
            if (titleText.getText().toString().equals("")) {
                RequestUtil.makeSnackBar(activity, view, "Введите название");
                return;
            }
            if (durationAutoCompleteTextView.getText().toString().equals("")) {
                RequestUtil.makeSnackBar(activity, view, "Выберите продолжительность слота");
                return;
            }
            if (pickedStartDate == null || pickedEndDate == null) {
                RequestUtil.makeSnackBar(activity, view, "Выберите начало и конец рабочего дня");
                return;
            }
            if (pickedStartDate.toEpochDay() >= pickedEndDate.toEpochDay()) {
                RequestUtil.makeSnackBar(activity, view, "Начало рабочего времени не может быть позже чем его конец");
                return;
            }
            createSchedule(titleText.getText().toString(), durationAutoCompleteTextView.getText().toString(),
                    daysList, checkboxFreeTimeText.getText().toString());
        });
        dialog.show();
    }

    private void setDateTimePickerToField(View scheduleView) {
        TextInputLayout dateRangeLayout = scheduleView.findViewById(R.id.create_schedule_date_range_layout);
        TextInputEditText dateRangeText = scheduleView.findViewById(R.id.create_schedule_date_range_text);
        TextInputLayout startTimeLayout = scheduleView.findViewById(R.id.create_schedule_time_start_layout);
        TextInputEditText startTimeText = scheduleView.findViewById(R.id.create_schedule_time_start_text);
        TextInputLayout endTimeLayout = scheduleView.findViewById(R.id.create_schedule_time_end_layout);
        TextInputEditText endTimeText = scheduleView.findViewById(R.id.create_schedule_time_end_text);
        TextInputLayout checkboxHolidayLayout = scheduleView.findViewById(R.id.create_schedule_checkbox_holiday_layout);
        TextInputEditText checkboxHolidayText = scheduleView.findViewById(R.id.create_schedule_checkbox_holiday_text);
        TextInputLayout checkboxFreeTimeLayout = scheduleView.findViewById(R.id.create_schedule_checkbox_free_time_layout);
        TextInputEditText checkboxFreeTimeText = scheduleView.findViewById(R.id.create_schedule_checkbox_free_time_text);


        dateRangeLayout.setEndIconOnClickListener(view -> {
            Long startDate = localDateToEpochMilli(LocalDate.now());
            Long endDate = localDateToEpochMilli(LocalDate.now().plusWeeks(1));

            if (pickedStartDate != null && pickedEndDate != null) {
                startDate = localDateToEpochMilli(pickedStartDate);
                endDate = localDateToEpochMilli(pickedEndDate);
            }

            CalendarConstraints.Builder calendarConstraints =
                    new CalendarConstraints.Builder();
            calendarConstraints.setStart(startDate);

            MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Выберите даты")
                    .setSelection(
                            new Pair<>(startDate, endDate)
                    )
                    .setCalendarConstraints(calendarConstraints.build())
                    .build();


            datePicker.show(fragmentManager, "DATE_PICKER");
            datePicker.addOnPositiveButtonClickListener(selection -> {
                pickedStartDate = epochMilliToLocalDate(selection.first);
                pickedEndDate = epochMilliToLocalDate(selection.second);
                dateRangeText.setText(pickedStartDate.toString() + " - " + pickedEndDate.toString());
            });
        });

        MaterialTimePicker.Builder timePickerBuilder = new MaterialTimePicker.Builder();
        timePickerBuilder.setTitleText("Выберите время");
        timePickerBuilder.setTimeFormat(TimeFormat.CLOCK_24H);
        timePickerBuilder.setMinute(0);

        startTimeLayout.setEndIconOnClickListener(view -> {
            timePickerBuilder.setHour(8);
            if (pickedStartTime != null) {
                timePickerBuilder.setHour(pickedStartTime.getHour());
                timePickerBuilder.setMinute(pickedStartTime.getMinute());
            }
            MaterialTimePicker timePicker = timePickerBuilder.build();

            timePicker.addOnPositiveButtonClickListener(v -> {
                pickedStartTime = LocalTime.of(timePicker.getHour(), timePicker.getMinute());
                startTimeText.setText(pickedStartTime.toString());
            });

            timePicker.show(fragmentManager, "TIME_PICKER");
        });

        endTimeLayout.setEndIconOnClickListener(view -> {
            timePickerBuilder.setHour(17);
            if (pickedEndTime != null) {
                timePickerBuilder.setHour(pickedEndTime.getHour());
                timePickerBuilder.setMinute(pickedEndTime.getMinute());
            }
            MaterialTimePicker timePicker = timePickerBuilder.build();

            timePicker.addOnPositiveButtonClickListener(v -> {
                pickedEndTime = LocalTime.of(timePicker.getHour(), timePicker.getMinute());
                endTimeText.setText(pickedEndTime.toString());
            });

            timePicker.show(fragmentManager, "TIME_PICKER");
        });

        boolean[] selectedDays = new boolean[7];
        daysList = new ArrayList<>();
        String[] days = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
        checkboxHolidayLayout.setEndIconOnClickListener(view -> {
            AlertDialog.Builder daysDialog = new AlertDialog.Builder(context);
            daysDialog.setCancelable(false);
            daysDialog.setTitle("Выберите нерабочие дни");

            daysDialog.setMultiChoiceItems(days, selectedDays, (dialogInterface, i, b) -> {
                if (b) {
                    daysList.add(i);
                    Collections.sort(daysList);
                } else {
                    daysList.remove(Integer.valueOf(i));
                }
            });

            daysDialog.setPositiveButton("OK", (dialogInterface, i) -> {
                StringBuilder stringBuilder = new StringBuilder();
                for (int j = 0; j < daysList.size(); j++) {
                    stringBuilder.append(days[daysList.get(j)]);
                    if (j != daysList.size() - 1) {
                        stringBuilder.append(", ");
                    }
                }
                checkboxHolidayText.setText(stringBuilder.toString());
            });

            daysDialog.setNegativeButton("Назад", (dialogInterface, i) -> dialogInterface.dismiss());

            daysDialog.show();
        });

        checkboxFreeTimeLayout.setEndIconOnClickListener(view -> {
            if (pickedStartTime == null || pickedEndTime == null) {
                RequestUtil.makeSnackBar(activity, view, "Выберите начало и конце активного времени");
                return;
            }
            AlertDialog.Builder daysDialog = new AlertDialog.Builder(context);
            daysDialog.setCancelable(false);
            daysDialog.setTitle("Выберите нерабочие время");
            int startFreeTime = pickedStartTime.getHour() + 1;
            int endFreeTime = pickedEndTime.getHour() - 1;
            if (endFreeTime - startFreeTime > 0) {
                String[] times = new String[endFreeTime - startFreeTime];
                boolean[] selectedTimes = new boolean[endFreeTime - startFreeTime];
                List<Integer> timeList = new ArrayList<>();
                for (int i = startFreeTime; i < endFreeTime; i++) {
                    times[i - startFreeTime] = i + ":00 - " + (i + 1) + ":00";
                }

                daysDialog.setMultiChoiceItems(times, selectedTimes, (dialogInterface, i, b) -> {
                    if (b) {
                        timeList.add(i);
                        Collections.sort(timeList);
                    } else {
                        timeList.remove(Integer.valueOf(i));
                    }
                });

                daysDialog.setPositiveButton("OK", (dialogInterface, i) -> {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int j = 0; j < timeList.size(); j++) {
                        stringBuilder.append(times[timeList.get(j)]);
                        if (j != timeList.size() - 1) {
                            stringBuilder.append(", ");
                        }
                    }
                    checkboxFreeTimeText.setText(stringBuilder.toString());
                });

                daysDialog.setNegativeButton("Назад", (dialogInterface, i) -> dialogInterface.dismiss());

                daysDialog.show();
            } else {
                RequestUtil.makeSnackBar(activity, view, "С такими настройками нельзя выбрать свободное время");
            }
        });
    }

    private void addScheduleDatesToSchedule(Schedule schedule, List<Integer> freeDaysInt, String freeTimesStr) {
        List<DayOfWeek> freeDays = freeDaysInt.stream().map(num -> DayOfWeek.of(++num)).collect(Collectors.toList());
        List<Pair<LocalTime, LocalTime>> freeTimes = parseFreeTimeStr(freeTimesStr);

        List<ScheduleDate> availableDates = new ArrayList<>();
        while (!pickedStartDate.isAfter(pickedEndDate)) {
            if (freeDays.contains(pickedStartDate.getDayOfWeek())) {
                pickedStartDate = pickedStartDate.plusDays(1);
                continue;
            }
            ScheduleDate date = new ScheduleDate();
            List<Slot> slots = new ArrayList<>();
            date.setDate(pickedStartDate);
            availableDates.add(date);
            LocalTime time = pickedStartTime;
            while (!time.isAfter(pickedEndTime) && !time.equals(pickedEndTime)) {
                LocalTime endTime = time.plus(schedule.getDurationOfOneSlot(), ChronoUnit.SECONDS);
                boolean slotInFreeTime = false;
                for (Pair<LocalTime, LocalTime> pair : freeTimes) {
                    if ((time.isAfter(pair.first) && time.isBefore(pair.second)) ||
                            (endTime.isAfter(pair.first) && endTime.isBefore(pair.second))) {
                        slotInFreeTime = true;
                        break;
                    }
                }
                if (!slotInFreeTime) {
                    Slot slot = new Slot();
                    slot.setStartTime(time);
                    slot.setEndTime(endTime);
                    slots.add(slot);
                }
                time = endTime;
            }
            date.setSlots(slots);
            pickedStartDate = pickedStartDate.plusDays(1);
        }

        List<LocalDate> localDateAlreadyExisting = schedule.getAvailableDates().stream()
                .map(ScheduleDate::getDate).collect(Collectors.toList());
        List<ScheduleDate> newAvailableDates =
                availableDates.stream().filter(localDateAlreadyExisting::contains).collect(Collectors.toList());
        schedule.setAvailableDates(availableDates);
        schedule.getAvailableDates().addAll(newAvailableDates);
        try {
            updateScheduleRequest(schedule);
        } catch (JsonProcessingException e) {
            RequestUtil.makeSnackBar(activity, view, e.getMessage());
        }
    }

    private void createSchedule(String title, String durationStr, List<Integer> freeDaysInt, String freeTimesStr) {
        Long duration = parseDurationStr(durationStr);
        List<DayOfWeek> freeDays = freeDaysInt.stream().map(num -> DayOfWeek.of(++num)).collect(Collectors.toList());
        List<Pair<LocalTime, LocalTime>> freeTimes = parseFreeTimeStr(freeTimesStr);
        List<ScheduleDate> availableDates = new ArrayList<>();
        while (!pickedStartDate.isAfter(pickedEndDate)) {
            if (freeDays.contains(pickedStartDate.getDayOfWeek())) {
                pickedStartDate = pickedStartDate.plusDays(1);
                continue;
            }
            ScheduleDate date = new ScheduleDate();
            List<Slot> slots = new ArrayList<>();
            date.setDate(pickedStartDate);
            availableDates.add(date);
            LocalTime time = pickedStartTime;
            while (!time.isAfter(pickedEndTime) && !time.equals(pickedEndTime)) {
                LocalTime endTime = time.plus(duration, ChronoUnit.SECONDS);
                boolean slotInFreeTime = false;
                for (Pair<LocalTime, LocalTime> pair : freeTimes) {
                    if ((time.isAfter(pair.first) && time.isBefore(pair.second)) ||
                            (endTime.isAfter(pair.first) && endTime.isBefore(pair.second))) {
                        slotInFreeTime = true;
                        break;
                    }
                }
                if (!slotInFreeTime) {
                    Slot slot = new Slot();
                    slot.setStartTime(time);
                    slot.setEndTime(endTime);
                    slots.add(slot);
                }
                time = endTime;
            }
            date.setSlots(slots);
            pickedStartDate = pickedStartDate.plusDays(1);
        }
        Schedule schedule = new Schedule();
        schedule.setTitle(title);
        schedule.setDurationOfOneSlot(duration);
        schedule.setAvailableDates(availableDates);
        if (organizationId != null) {
            schedule.setOrganizationId(organizationId);
        }
        try {
            createScheduleRequest(schedule);
        } catch (JsonProcessingException e) {
            RequestUtil.makeSnackBar(activity, view, e.getMessage());
        }
    }

    private List<Pair<LocalTime, LocalTime>> parseFreeTimeStr(String freeTimeStr) {
        if (freeTimeStr.isEmpty()) {
            return Collections.emptyList();
        }
        List<Pair<LocalTime, LocalTime>> freeTime = new ArrayList<>();
        String[] splitFreeTimeStr = freeTimeStr.split(", ");
        for (int i = 0; i < splitFreeTimeStr.length; i++) {
            String[] splitTimeRange = splitFreeTimeStr[i].split(" - ");
            int start_hour = Integer.parseInt(splitTimeRange[0].split(":")[0]);
            int end_hour = Integer.parseInt(splitTimeRange[1].split(":")[0]);
            Pair<LocalTime, LocalTime> pair = new Pair<>(LocalTime.of(start_hour, 0),
                    LocalTime.of(end_hour, 0));
            freeTime.add(pair);
        }
        return freeTime;
    }

    private Long parseDurationStr(String durationStr) {
        int hour = 0;
        int minutes = 0;
        String[] splitDurationStr = durationStr.split(" ");
        if (splitDurationStr.length == 2) {
            hour = Integer.parseInt(splitDurationStr[0].substring(0, splitDurationStr[0].length() - 1));
            minutes = Integer.parseInt(splitDurationStr[1].substring(0, splitDurationStr[1].length() - 1));
        } else {
            minutes = Integer.parseInt(durationStr.substring(0, durationStr.length() - 1));
        }
        return Duration.ofHours(hour).plus(Duration.ofMinutes(minutes)).toMillis() / 1000;
    }

    private Long localDateToEpochMilli(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC)
                .toInstant().toEpochMilli();
    }

    private LocalDate epochMilliToLocalDate(Long epochMilli) {
        return Instant.ofEpochMilli(epochMilli)
                .atZone(ZoneOffset.UTC).toLocalDate();
    }

    public void updateScheduleRequest(Schedule schedule) throws JsonProcessingException {
        RequestBody body = RequestBody.create(
                RequestUtil.OBJECT_MAPPER.writeValueAsString(schedule), RequestUtil.MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(RequestUtil.BASE_SCHEDULE_URL)
                .put(body)
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
                ScheduleFragment scheduleFragment = new ScheduleFragment();
                Bundle arguments = new Bundle();
                arguments.putString("id", schedule.getId().toString());
                scheduleFragment.setArguments(arguments);
                fragmentManager.beginTransaction().replace(R.id.fragment_container_view,
                        scheduleFragment, "SCHEDULE").commit();
            }
        });
    }

    public void createScheduleRequest(Schedule schedule) throws JsonProcessingException {
        RequestBody body = RequestBody.create(
                RequestUtil.OBJECT_MAPPER.writeValueAsString(schedule), RequestUtil.MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(RequestUtil.BASE_SCHEDULE_URL)
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
                if (!response.isSuccessful()) {
                    RequestUtil.makeSnackBar(activity, view, response.message());
                }
                if (schedule.getOrganizationId() != null) {
                    OrganizationFragment organizationFragment = new OrganizationFragment();
                    Bundle arguments = new Bundle();
                    arguments.putString("id", schedule.getOrganizationId().toString());
                    organizationFragment.setArguments(arguments);
                    activity.runOnUiThread(() -> {
                        fragmentManager.beginTransaction().replace(R.id.fragment_container_view,
                                organizationFragment, "ORGANIZATION").commit();
                    });
                } else {
                    activity.runOnUiThread(() -> {
                        fragmentManager.beginTransaction()
                                .replace(R.id.fragment_container_view, ScheduleListFragment.class, null).commit();
                    });
                }
            }
        });
    }

    public void deleteScheduleRequest(UUID id) {
        Request request = new Request.Builder()
                .url(RequestUtil.BASE_SCHEDULE_URL + "/" + id)
                .delete()
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
                if (organizationId != null) {
                    OrganizationFragment organizationFragment = new OrganizationFragment();
                    Bundle arguments = new Bundle();
                    arguments.putString("id", organizationId.toString());
                    organizationFragment.setArguments(arguments);
                    activity.runOnUiThread(() -> {
                        fragmentManager.beginTransaction().replace(R.id.fragment_container_view,
                                organizationFragment, "ORGANIZATION").commit();
                    });
                } else {
                    activity.runOnUiThread(() -> {
                        fragmentManager.beginTransaction()
                                .replace(R.id.fragment_container_view, ScheduleListFragment.class, null).commit();
                    });
                }
            }
        });
    }
}
