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
import com.easybook.entity.Organization;
import com.easybook.util.RequestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class OrganizationListFragment extends Fragment {
    public OrganizationListFragment() {
        super(R.layout.organization_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = this.getActivity();

        String token = activity.
                getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", "");

        Request request = new Request.Builder()
                .url(RequestUtil.BASE_ORGANIZATION_URL)
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
                    RecyclerView recyclerView = view.findViewById(R.id.organization_list);
                    String respStr = response.body().string();
                    List<Organization> organizations =
                            RequestUtil.OBJECT_MAPPER.readValue(respStr,
                                    new TypeReference<List<Organization>>() {});
                    OrganizationAdapter organizationAdapter = new OrganizationAdapter(view.getContext(),
                            organizations,
                            getParentFragmentManager());
                    activity.runOnUiThread(() -> {
                        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                                DividerItemDecoration.VERTICAL));
                        recyclerView.setAdapter(organizationAdapter);
                    });
                } catch (Exception e) {
                    Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}
