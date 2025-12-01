package com.example.lotterize;


import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a helper that reads Firestore and builds a CSV string
 * for the event’s final/enrolled entrants.
 * CSV columns: {@code name,userName,userId,email,phoneNumber}
 */
public final class EventCsvExporter {
    public EventCsvExporter(){}

    /**
     * This defines a callback interface for returning the CSV result asynchronously.
     */
    public interface Callback {
        /**
         * This is called when the CSV has been successfully built (header-only if no data).
         * @param csv CSV content encoded as text.
         */
        void onSuccess(@NonNull String csv);

        /**
         * This is called when Firestore access or CSV assembly fails.
         * @param message A short error description.
         */
        void onError(@NonNull String message);
    }

    /**
     * This reads {@code /events/{eventId}} to obtain {@code finalList} (user IDs),
     * fetches user docs in chunks of 10 via {@code whereIn(documentId, ...)},
     * and returns a CSV string through {@link Callback}.
     *
     * @param db       Firestore instance
     * @param eventId  Event document ID
     * @param cb       Callback receiving CSV on success or an error message on failure
     */
    public static void buildEnrolledCsv(FirebaseFirestore db, String eventId, Callback cb){
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(snap ->{
                    if (snap == null || !snap.exists()){
                        cb.onSuccess(header());
                        return;
                    }

                    Object raw = snap.get("finalList");
                    ArrayList<String> ids = new ArrayList<>();

                    if (raw instanceof List<?>) {
                        for (Object o : (List<?>) raw) {
                            if (o != null) ids.add(String.valueOf(o));
                        }
                    }

                    ArrayList<Task<QuerySnapshot>> usersById = new ArrayList<>();
                    for(int i = 0 ; i < ids.size(); i += 10){ //Firestore in/whereIn limit is 10
                        List<String> chunk = ids.subList(i, Math.min(i + 10, ids.size()));
                        usersById.add(db.collection("users").whereIn(FieldPath.documentId(), chunk).get());
                    }

                    Tasks.whenAllSuccess(usersById)
                            .addOnSuccessListener(results ->{
                                Map<String, Map<String, Object>> mapById = new HashMap<>();
                                for (Object o:results){
                                    for (DocumentSnapshot ds: ((QuerySnapshot)o)){
                                        mapById.put(ds.getId(), ds.getData() == null ? Collections.emptyMap(): ds.getData());
                                    }
                                }
                                cb.onSuccess((buildCsv(ids, mapById)));
                    })
                            .addOnFailureListener(exception ->{
                                cb.onError("Failed to load users: " + exception.getMessage());
                            });

                })
                .addOnFailureListener(exception ->{
                   cb.onError("Failed to load event: " + exception.getMessage());
                });
    }

    /**
     * This returns the CSV header row (with a trailing newline).
     * @return a string represents the header of csv file
     */
    private static String header() {
        return "name,userName,userId,email,phoneNumber\n";
    }

    /**
     * This assembles the CSV body (including the header) from the given user IDs
     * and the fetched user field map. Missing fields are written as empty strings.
     *
     * @param ids       Ordered list of user document IDs
     * @param mapByIds  Map of {@code userId -> user fields} from Firestore
     * @return A complete CSV string ready to write
     */
    private static String buildCsv(ArrayList<String> ids, Map<String, Map<String, Object>> mapByIds){
        StringBuilder s = new StringBuilder(header());
        for (String id : ids){
            Map<String, Object> user = mapByIds.get(id);
            String name = getField(user, "name");
            String userName = getField(user, "username");
            String userId = getField(user, "userId");
            String email = getField(user, "email");
            String phoneNumber = getField(user,"phoneNumber");
            s.append(csv(name)).append(",")
                    .append(csv(userName)).append(",")
                    .append(csv(userId)).append(",")
                    .append(csv(email)).append(",")
                    .append(csv(phoneNumber)).append("\n");
        }
        return s.toString();
    }

    /**
     * This safely extracts a string field from a user map; returns {@code ""} if missing/null.
     *
     * @param user  User document data (may be {@code null})
     * @param field Field key to read
     * @return Field value as a string or {@code ""} if absent
     */
    private static String getField(Map<String, Object> user, String field){
        if (user == null){
            return "";
        }
        Object s = user.get(field);
        return s != null && s != "" ? String.valueOf(s):"";
    }

    /**
     * This CSV-escapes a value: wraps in quotes if it contains comma/quote/newline,
     * and doubles any inner quotes.
     *
     * @param s Input string (may be {@code null})
     * @return Escaped CSV cell text
     */
    private static String csv(String s) {
        if (s == null) s = "";
        boolean needs = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String v = s.replace("\"", "\"\"");
        return needs ? ("\"" + v + "\"") : v;
    }
}
