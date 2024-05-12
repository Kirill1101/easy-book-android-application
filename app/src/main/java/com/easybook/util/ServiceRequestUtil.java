package com.easybook.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;

import com.easybook.R;
import com.easybook.entity.Service;
import com.easybook.fragment.OrganizationFragment;
import com.easybook.fragment.ScheduleListFragment;
import com.easybook.fragment.ServiceListFragment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ServiceRequestUtil {
    private String token;
    private Activity activity;
    private Context context;
    private View view;
    private FragmentManager fragmentManager;
    private String scheduleId;

    public ServiceRequestUtil(String token, Activity activity, Context context,
                              View view, FragmentManager fragmentManager, String scheduleId) {
        this.token = token;
        this.activity = activity;
        this.context = context;
        this.view = view;
        this.fragmentManager = fragmentManager;
        this.scheduleId = scheduleId;
    }

    public void showCreateServiceWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);

        LayoutInflater inflater = LayoutInflater.from(context);
        View createOrganizationWindow = inflater.inflate(R.layout.create_service, null);
        dialog.setView(createOrganizationWindow);

        TextInputEditText titleText = createOrganizationWindow.findViewById(R.id.create_service_title_text);
        TextInputEditText durationText = createOrganizationWindow.findViewById(R.id.create_service_duration_text);
        TextInputEditText priceText = createOrganizationWindow.findViewById(R.id.create_service_price_text);
        Slider slider = createOrganizationWindow.findViewById(R.id.create_service_price_slider);

        slider.addOnChangeListener((slider1, value, fromUser) -> {
            durationText.setText(String.valueOf(value));
        });

        dialog.setNegativeButton("Назад", (dialogInterface, i) -> dialogInterface.dismiss());

        dialog.setPositiveButton("Создать", (dialogInterface, i) -> {
            if (titleText.getText().toString().equals("")) {
                RequestUtil.makeSnackBar(activity, view, "Введите название");
                return;
            }
            if (durationText.getText().toString().equals("")) {
                RequestUtil.makeSnackBar(activity, view, "Выберите продолжительность услуги");
                return;
            }
            if (priceText.getText().toString().equals("")) {
                RequestUtil.makeSnackBar(activity, view, "Выберите стоимость услуги");
                return;
            }

            Service service = new Service();
            service.setTitle(titleText.getText().toString());
            service.setDuration(Float.valueOf(durationText.getText().toString()).longValue() * 60);
            service.setPrice(Integer.valueOf(priceText.getText().toString()));
            service.setScheduleId(UUID.fromString(scheduleId));
            try {
                createServiceRequest(service);
            } catch (JsonProcessingException e) {
                RequestUtil.makeSnackBar(activity, view, e.getMessage());
            }
        });
        dialog.show();
    }

    private void createServiceRequest(Service service) throws JsonProcessingException {
        RequestBody body = RequestBody.create(
                RequestUtil.OBJECT_MAPPER.writeValueAsString(service), RequestUtil.MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(RequestUtil.BASE_SERVICE_URL)
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
                ServiceListFragment serviceListFragment = new ServiceListFragment();
                Bundle arguments = new Bundle();
                arguments.putString("scheduleId", scheduleId);
                serviceListFragment.setArguments(arguments);
                activity.runOnUiThread(() -> {
                    fragmentManager.beginTransaction().replace(R.id.fragment_container_view,
                            serviceListFragment, "ORGANIZATION").commit();
                });
            }
        });
    }

    public void deleteServiceRequest(UUID id) {
        Request request = new Request.Builder()
                .url(RequestUtil.BASE_SERVICE_URL + "/" + id)
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
                ServiceListFragment serviceListFragment = new ServiceListFragment();
                Bundle arguments = new Bundle();
                arguments.putString("scheduleId", scheduleId);
                serviceListFragment.setArguments(arguments);
                activity.runOnUiThread(() -> {
                    fragmentManager.beginTransaction().replace(R.id.fragment_container_view,
                            serviceListFragment, "ORGANIZATION").commit();
                });
            }
        });
    }
}
