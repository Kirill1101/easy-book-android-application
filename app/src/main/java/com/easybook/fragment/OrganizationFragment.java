package com.easybook.fragment;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import com.easybook.adapter.ScheduleAdapter;
import com.easybook.entity.Organization;
import com.easybook.util.RequestUtil;
import com.easybook.util.ScheduleRequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrganizationFragment extends Fragment {

    private Organization organization;

    private ScheduleAdapter scheduleAdapter;

    private String token;

    private ScheduleRequestUtil scheduleRequestUtil;

    public OrganizationFragment() {
        super(R.layout.organization);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.organization_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
                .url(RequestUtil.BASE_ORGANIZATION_URL + "/" + getArguments().getString("id"))
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
                    TextView organizationTitle = view.findViewById(R.id.organization_title);
                    String respStr = response.body().string();
                    organization = RequestUtil.OBJECT_MAPPER.readValue(respStr,
                            Organization.class);
                    scheduleRequestUtil.setOrganizationId(organization.getId());
                    scheduleAdapter = new ScheduleAdapter(view.getContext(),
                            organization.getSchedules(),
                            getParentFragmentManager());
                    activity.runOnUiThread(() -> {
                        String title = "Организация: " + organization.getTitle();
                        registerForContextMenu(recyclerView);
                        organizationTitle.setText(title);
                        recyclerView.setAdapter(scheduleAdapter);
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
            case R.id.organization_menu_edit: {
                dialogEditOrganization();
                break;
            }
            case R.id.organization_menu_share:
                dialogShare();
                break;
            case R.id.organization_menu_create_schedule:
                dialogCreateSchedule();
                break;
        }
        return false;
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

    private void updateOrganization(Organization organization) throws JsonProcessingException {
        RequestBody body = RequestBody.create(
                RequestUtil.OBJECT_MAPPER.writeValueAsString(organization), RequestUtil.MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(RequestUtil.BASE_ORGANIZATION_URL)
                .put(body)
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
                OrganizationFragment organizationFragment = new OrganizationFragment();
                Bundle arguments = new Bundle();
                arguments.putString("id", organization.getId().toString());
                organizationFragment.setArguments(arguments);
                getActivity().runOnUiThread(() -> {
                    getParentFragmentManager().beginTransaction().replace(R.id.fragment_container_view,
                            organizationFragment, "ORGANIZATION").commit();
                });
            }
        });
    }

    private void dialogEditOrganization() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Редактирование организации");
        dialog.setMessage("Введите необходимые данные");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View editOrganizationWindow = inflater.inflate(R.layout.create_organization, null);
        dialog.setView(editOrganizationWindow);

        final MaterialEditText title = editOrganizationWindow.findViewById(R.id.field_create_organization);
        title.setText(organization.getTitle());

        dialog.setNegativeButton("Назад", (dialogInterface, i) -> dialogInterface.dismiss());

        dialog.setPositiveButton("Сохранить", (dialogInterface, i) -> {
            if (TextUtils.isEmpty(Objects.requireNonNull(title.getText()).toString())) {
                RequestUtil.makeSnackBar(getActivity(), getView(), "Введите название");
                return;
            }

            organization.setTitle(title.getText().toString());

            try {
                updateOrganization(organization);
            } catch (JsonProcessingException e) {
                RequestUtil.makeSnackBar(getActivity(), getView(), e.getMessage());
            }
        });
        dialog.show();
    }

    private void dialogShare() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Идентификатор организации");
        dialog.setMessage("Поделитесь с теми, кому хотите дать доступ для записи");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View shareIdWindow = inflater.inflate(R.layout.share_id_window, null);
        dialog.setView(shareIdWindow);
        final MaterialEditText organizationId = shareIdWindow.findViewById(R.id.text_share_id);
        organizationId.setText(organization.getId().toString());

        dialog.setNegativeButton("Назад", (dialogInterface, i) -> dialogInterface.dismiss());

        dialog.setPositiveButton("Копировать", (dialogInterface, i) -> {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", organization.getId().toString());
            clipboard.setPrimaryClip(clip);
        });

        dialog.show();
    }

    private void dialogCreateSchedule() {
        scheduleRequestUtil.showCreateScheduleWindow();
    }
}
