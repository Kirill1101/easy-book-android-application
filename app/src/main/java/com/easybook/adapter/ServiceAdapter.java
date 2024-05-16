package com.easybook.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.easybook.R;
import com.easybook.entity.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder>{
    private final LayoutInflater inflater;

    private final List<Service> services;

    private final FragmentManager fragmentManager;

    private int contextPosition;

    public ServiceAdapter(Context context, List<Service> services,
                              FragmentManager fragmentManager) {
        this.services = services;
        this.inflater = LayoutInflater.from(context);
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ServiceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.service_list_item, parent, false);
        return new ServiceAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceAdapter.ViewHolder holder, int position) {
        Service service = services.get(position);

        holder.title.setText(service.getTitle());
        holder.duration.setText(Duration.ofSeconds(service.getDuration())
                .toMinutes() + " минут");
        holder.price.setText(service.getPrice().toString());

        holder.itemView.setOnLongClickListener((view) -> {
            contextPosition = position;
            return false;
        });
    }

    public Service getCurrentItem() {
        return services.get(contextPosition);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public UUID deleteService() {
        UUID id = services.get(contextPosition).getId();
        services.remove(contextPosition);
        return id;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title, duration, price;

        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.service_item_title);
            duration = view.findViewById(R.id.service_item_duration);
            price = view.findViewById(R.id.service_item_price);
        }
    }
}
