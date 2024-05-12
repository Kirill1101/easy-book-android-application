package com.easybook.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.easybook.R;
import com.easybook.entity.Organization;
import com.easybook.fragment.OrganizationFragment;
import com.easybook.util.RequestUtil;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.UUID;

public class OrganizationAdapter extends RecyclerView.Adapter<OrganizationAdapter.ViewHolder> {
    private final LayoutInflater inflater;

    private final List<Organization> organizations;

    private final FragmentManager fragmentManager;

    private int contextPosition;

    public OrganizationAdapter(Context context, List<Organization> organizations,
                               FragmentManager fragmentManager) {
        this.organizations = organizations;
        this.inflater = LayoutInflater.from(context);
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public OrganizationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.organization_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizationAdapter.ViewHolder holder, int position) {
        Organization organization = organizations.get(position);
        holder.title.setText(organization.getTitle());
        holder.itemView.setOnClickListener((view) -> {
            OrganizationFragment organizationFragment = new OrganizationFragment();
            Bundle arguments = new Bundle();
            arguments.putString("id", organization.getId().toString());
            organizationFragment.setArguments(arguments);
            fragmentManager.beginTransaction().replace(R.id.fragment_container_view,
                    organizationFragment, "ORGANIZATION").commit();
        });

        holder.itemView.setOnLongClickListener((view) -> {
            contextPosition = position;
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return organizations.size();
    }

    public UUID deleteOrganization() {
        UUID id = organizations.get(contextPosition).getId();
        organizations.remove(contextPosition);
        return id;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        ViewHolder(View view){
            super(view);
            title = view.findViewById(R.id.organization_item_title);
        }
    }
}
