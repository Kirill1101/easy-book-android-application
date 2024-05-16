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
import com.easybook.adapter.ServiceAdapter;
import com.easybook.entity.Service;
import com.easybook.util.RequestUtil;
import com.easybook.util.ServiceRequestUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ServiceListFragment extends Fragment {
    private ServiceAdapter serviceAdapter;

    private String token;

    private ServiceRequestUtil serviceRequestUtil;

    public ServiceListFragment(){
        super(R.layout.list);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.service_list_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        Activity activity = this.getActivity();

        token = activity.
                getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", "");
        serviceRequestUtil = new ServiceRequestUtil(token, getActivity(),
                getContext(), getView(), getParentFragmentManager(), getArguments().getString("scheduleId"));
        Request request = new Request.Builder()
                .url(RequestUtil.BASE_SCHEDULE_URL + "/" + getArguments().getString("scheduleId") + "/services")
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
                    RecyclerView recyclerView = view.findViewById(R.id.list);
                    String respStr = response.body().string();
                    List<Service> services =
                            RequestUtil.OBJECT_MAPPER.readValue(respStr,
                                    new TypeReference<List<Service>>() {});
                    serviceAdapter = new ServiceAdapter(view.getContext(),
                            services,
                            getParentFragmentManager());
                    activity.runOnUiThread(() -> {
                        if (services.size() == 0) {
                            TextView notExistingView = view.findViewById(R.id.not_existing_message);
                            notExistingView.setText("В этом расписании пока нет услуг");
                            notExistingView.setVisibility(View.VISIBLE);
                        }
                        recyclerView.setAdapter(serviceAdapter);
                        registerForContextMenu(recyclerView);
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
        menu.add(Menu.NONE, 1, Menu.NONE, "Редактировать");
        menu.add(Menu.NONE, 2, Menu.NONE, "Удалить");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                serviceRequestUtil.showEditServiceWindow(serviceAdapter.getCurrentItem());
                break;
            case 2:
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle("Вы уверены что хотите удалить услугу?");
                dialog.setNegativeButton("Нет", (dialogInterface, i) -> dialogInterface.dismiss());
                dialog.setPositiveButton("Да", (dialogInterface, i) -> {
                    UUID id = serviceAdapter.deleteService();
                    serviceRequestUtil.deleteServiceRequest(id);
                });
                dialog.show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.service_list_menu_create_service: {
                serviceRequestUtil.showCreateServiceWindow();
                break;
            }
        }
        return false;
    }
}
