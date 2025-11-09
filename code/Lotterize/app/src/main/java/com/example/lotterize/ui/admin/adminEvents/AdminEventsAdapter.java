package com.example.lotterize.ui.admin.adminEvents;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterize.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminEventsAdapter extends RecyclerView.Adapter<AdminEventsAdapter.ViewHolder> {

    private final Context context;
    private final List<Map<String, Object>> eventsList;
    private final FirebaseFirestore db;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public AdminEventsAdapter(Context context, List<Map<String, Object>> eventsList) {
        this.context = context;
        this.eventsList = eventsList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> event = eventsList.get(position);
        String eventId = (String) event.get("eventId");
        String name = (String) event.get("eventName");

        String deadline = "";
        if (event.get("registrationDeadline") instanceof Timestamp) {
            Timestamp ts = (Timestamp) event.get("registrationDeadline");
            deadline = dateFormat.format(ts.toDate());
        }

        holder.textTitle.setText(name != null ? name : "Untitled Event");
        holder.textDate.setText(deadline.isEmpty() ? "No deadline" : deadline);

        holder.buttonViewDetails.setOnClickListener(v -> showEventDetailsDialog(event));

        holder.buttonDelete.setOnClickListener(v -> {
            if (eventId != null) {
                db.collection("events").document(eventId)
                        .delete()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show();
                            eventsList.remove(position);
                            notifyItemRemoved(position);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show()
                        );
            } else {
                Toast.makeText(context, "Event ID missing", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEventDetailsDialog(Map<String, Object> event) {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(event.get("eventName")).append("\n");
        details.append("Description: ").append(event.get("description")).append("\n");
        details.append("Location: ").append(event.get("location")).append("\n");

        details.append("Date: ");
        if (event.get("date") instanceof Timestamp) {
            details.append(dateFormat.format(((Timestamp) event.get("date")).toDate()));
        }
        details.append("\n");

        details.append("Registration Start: ");
        if (event.get("registrationStart") instanceof Timestamp) {
            details.append(dateFormat.format(((Timestamp) event.get("registrationStart")).toDate()));
        }
        details.append("\n");

        details.append("Deadline: ");
        if (event.get("registrationDeadline") instanceof Timestamp) {
            details.append(dateFormat.format(((Timestamp) event.get("registrationDeadline")).toDate()));
        }
        details.append("\n");

        details.append("Entrants Limit: ").append(event.get("entrantsLimit")).append("\n");
        details.append("Total Spots: ").append(event.get("totalSpots")).append("\n");
        details.append("QR Code: ").append(event.get("qrCode")).append("\n");
        details.append("Owner ID: ").append(event.get("ownerId")).append("\n");

        new AlertDialog.Builder(context)
                .setTitle("Event Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textDate;
        Button buttonDelete, buttonViewDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_title);
            textDate = itemView.findViewById(R.id.text_date);
            buttonDelete = itemView.findViewById(R.id.button_delete_event);
            buttonViewDetails = itemView.findViewById(R.id.button_view_details);
        }
    }
}
