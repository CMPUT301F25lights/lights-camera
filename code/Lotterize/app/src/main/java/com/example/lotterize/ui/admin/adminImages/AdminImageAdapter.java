package com.example.lotterize.ui.admin.adminImages;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lotterize.Event;
import com.example.lotterize.R;

import java.util.List;

public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ImageViewHolder> {

    private final OnDeleteClickListener listener;
    private List<Event> eventList;


    public interface OnDeleteClickListener {
        void onDeleteClick(Event event);
    }

    public AdminImageAdapter(List<Event> events, OnDeleteClickListener listener) {
        this.eventList = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Event event = eventList.get(position);

        Glide.with(holder.imageView.getContext())
                .load(event.getImageUrl())
                .into(holder.imageView);

        holder.eventName.setText(event.getEventName());

        // Set click listener for deletion
        holder.imageView.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Image")
                    .setMessage("Delete image for " + event.getEventName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        listener.onDeleteClick(event);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView eventName;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            eventName = itemView.findViewById(R.id.eventName);
        }
    }

    public void updateData(List<Event> events) {
        this.eventList = events;
        notifyDataSetChanged();
    }

    public void removeEvent(Event event) {
        int position = eventList.indexOf(event);
        if (position != -1) {
            eventList.remove(position);
            notifyItemRemoved(position);
        }
    }

}
