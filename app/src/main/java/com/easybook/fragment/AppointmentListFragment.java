package com.easybook.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.easybook.R;
import com.easybook.adapter.AppointmentAdapter;
import com.easybook.adapter.OrganizationAdapter;
import com.easybook.entity.Appointment;
import com.easybook.entity.Organization;
import com.easybook.util.RequestUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class AppointmentListFragment extends Fragment {
    public AppointmentListFragment(){
        super(R.layout.appointment_list);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.appointment_list_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        Activity activity = this.getActivity();

        String token = activity.
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
                if (!response.isSuccessful()) {
                    RequestUtil.makeSnackBar(activity, view, response.message());
                }
                try {
                    if (!response.isSuccessful()) {
                        RequestUtil.makeSnackBar(activity, view, response.message());
                    }
                    RecyclerView recyclerView = view.findViewById(R.id.appointment_list);
                    String respStr = response.body().string();
                    List<Appointment> appointments =
                            RequestUtil.OBJECT_MAPPER.readValue(respStr,
                                    new TypeReference<List<Appointment>>() {});
                    AppointmentAdapter organizationAdapter = new AppointmentAdapter(view.getContext(),
                            appointments,
                            getParentFragmentManager());
                    activity.runOnUiThread(() -> {
                        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                                DividerItemDecoration.VERTICAL));
                        recyclerView.setAdapter(organizationAdapter);
                    });
                } catch (Exception e) {
                    RequestUtil.makeSnackBar(activity, view, e.getMessage());
                }
            }
        });
    }
}
