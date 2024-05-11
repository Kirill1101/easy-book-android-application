package com.easybook.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.easybook.R;
import com.easybook.adapter.OrganizationAdapter;
import com.easybook.entity.Organization;
import com.easybook.entity.UserCredential;
import com.easybook.util.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.android.material.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrganizationListFragment extends Fragment {
    public OrganizationListFragment() {
        super(R.layout.organization_list);
    }

    private String token;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.organization_list_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        Activity activity = this.getActivity();

        token = activity.
                getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", "");

        Request request = new Request.Builder()
                .url(RequestUtil.BASE_ORGANIZATION_URL)
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
                    RecyclerView recyclerView = view.findViewById(R.id.organization_list);
                    String respStr = response.body().string();
                    List<Organization> organizations =
                            RequestUtil.OBJECT_MAPPER.readValue(respStr,
                                    new TypeReference<List<Organization>>() {});
                    OrganizationAdapter organizationAdapter = new OrganizationAdapter(view.getContext(),
                            organizations,
                            getParentFragmentManager());
                    activity.runOnUiThread(() -> {
                        //recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                        //        DividerItemDecoration.VERTICAL));
                        recyclerView.setAdapter(organizationAdapter);
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
            case R.id.organization_list_menu_create_organization: {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle("Создание организации");
                dialog.setMessage("Введите необходимые данные");

                LayoutInflater inflater = LayoutInflater.from(getContext());
                View createOrganizationWindow = inflater.inflate(R.layout.create_organization, null);
                dialog.setView(createOrganizationWindow);

                final MaterialEditText title = createOrganizationWindow.findViewById(R.id.field_create_organization);

                dialog.setNegativeButton("Назад", (dialogInterface, i) -> dialogInterface.dismiss());

                dialog.setPositiveButton("Создать", (dialogInterface, i) -> {
                    if (TextUtils.isEmpty(Objects.requireNonNull(title.getText()).toString())) {
                        RequestUtil.makeSnackBar(getActivity(), getView(), "Введите название");
                        return;
                    }

                    Organization organization = new Organization();
                    organization.setTitle(title.getText().toString());

                    try {
                        createOrganization(organization);
                    } catch (JsonProcessingException e) {
                        RequestUtil.makeSnackBar(getActivity(), getView(), e.getMessage());
                    }
                });
                dialog.show();
                break;
            }
        }
        return false;
    }

    private void createOrganization(Organization organization) throws JsonProcessingException{
        RequestBody body = RequestBody.create(
                RequestUtil.OBJECT_MAPPER.writeValueAsString(organization), RequestUtil.MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(RequestUtil.BASE_ORGANIZATION_URL)
                .post(body)
                .header("Authorization", token)
                .build();
        RequestUtil.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                RequestUtil.makeSnackBar(getActivity(), getView(), e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    RequestUtil.makeSnackBar(getActivity(), getView(), response.message());
                }
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container_view,
                        OrganizationListFragment.class, null).commit();
            }
        });
    }
}
