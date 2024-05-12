package com.easybook.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.easybook.R;
import com.easybook.entity.Organization;
import com.easybook.entity.Schedule;
import com.easybook.fragment.OrganizationFragment;
import com.easybook.fragment.ScheduleFragment;

import java.util.List;
import java.util.UUID;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder>{
    private final LayoutInflater inflater;
    private final List<Schedule> schedules;
    private final FragmentManager fragmentManager;
    private int contextPosition;

    public ScheduleAdapter(Context context, List<Schedule> schedules,
                               FragmentManager fragmentManager) {
        this.schedules = schedules;
        this.inflater = LayoutInflater.from(context);
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ScheduleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.schedule_list_item, parent, false);
        return new ScheduleAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleAdapter.ViewHolder holder, int position) {
        Schedule schedule = schedules.get(position);
        holder.title.setText(schedule.getTitle());
        holder.itemView.setOnClickListener((view) -> {
            ScheduleFragment scheduleFragment = new ScheduleFragment();
            Bundle arguments = new Bundle();
            arguments.putString("id", schedule.getId().toString());
            scheduleFragment.setArguments(arguments);
            fragmentManager.beginTransaction().replace(R.id.fragment_container_view,
                    scheduleFragment, "SCHEDULE").commit();
        });

        holder.itemView.setOnLongClickListener((view) -> {
            contextPosition = position;
            return false;
        });
    }

    public UUID deleteSchedule() {
        UUID id = schedules.get(contextPosition).getId();
        schedules.remove(contextPosition);
        return id;
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        ViewHolder(View view){
            super(view);
            title = view.findViewById(R.id.schedule_item_title);
        }
    }
}
