package com.example.lotterize.ui.addEvents;

import androidx.annotation.NonNull;

import com.example.lotterize.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the repository responsible for reading {@link User} documents from Firestore.
 * This class is implemented as a simple singleton so that production code
 * and tests can share a single entry point for user-related Firestore access.
 */
public class UsersRepository {
    private static UsersRepository instance = new UsersRepository();
    protected final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Returns the shared {@link UsersRepository} instance.
     *
     * @return the singleton instance
     */
    public static UsersRepository getInstance() {
        return instance;
    }

    /**
     * Replaces the current singleton instance.
     * This is mainly intended for tests
     *
     * @param userRepo the repository instance to use for subsequent
     *                 {@link #getInstance()} calls
     */
    public static void setInstance(UsersRepository userRepo) {
        instance = userRepo;
    }

    /**
     * This is the callback used when resolving a list of display names from user IDs.
     */
    public interface NamesCallback{

        /**
         * This is called when display names are successfully resolved.
         *
         * @param displayNames list of display names corresponding to the requested
         *                     user IDs; if a user has no stored name, the raw ID
         *                     is used instead.
         */
        void onSuccess(@NonNull ArrayList<String> displayNames);

        /**
         * Called when an error occurs while resolving names.
         * In case of failure, {@code fallbackIds} contains the raw user IDs
         * so that the caller can still render something in the UI.
         *
         * @param e          the exception describing the failure
         * @param fallbackIds the original user IDs to use as a fallback
         */
        void onError(@NonNull Exception e, @NonNull ArrayList<String> fallbackIds);
    }

    /**
     * This is the callback for queries that return a list of {@link User} objects.
     */
    public interface UsersCallback{
        /**
         * This is called when users are successfully loaded.
         *
         * @param users list of {@link User} objects corresponding to the requested IDs;
         *              may be empty if none match
         */
        void onSuccess(@NonNull ArrayList<User> users);

        /**
         * Called when an error occurs while loading users.
         *
         * @param e the exception describing the failure
         */
        void onError(@NonNull Exception e);
    }

    /**
     * This loads all users and returns their display names given list of userId
     * (falling back to the raw ID when no name is present).
     * This implementation currently fetches the entire {@code users} collection,
     * builds a map from user ID to name, and then applies that mapping to the
     * provided {@code docIds} using
     * {@link EntrantListFragment#applyNames(ArrayList, Map)}.
     *
     * @param docIds   list of user document IDs whose display names should be resolved
     * @param callback callback to receive resolved names or a fallback in case of error
     */
    public void resolveDisplayNames(@NonNull ArrayList<String> docIds, @NonNull NamesCallback callback) {
        db.collection("users")
                .get()
                .addOnSuccessListener(qs -> {
                    Map<String,String> idToNameMap = new HashMap<>();
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        String id   = d.getId();
                        String name = d.getString("name");
                        idToNameMap.put(id, (name != null && !name.isEmpty()) ? name : id);
                    }
                    ArrayList<String> names = EntrantListFragment.applyNames(docIds, idToNameMap);
                    callback.onSuccess(names);
                })
                .addOnFailureListener(e -> {
                    // If we fail, just use the raw IDs (like your current code)
                    callback.onError(e, new ArrayList<>(docIds));
                });
    }

    /**
     * This fetches {@link User} documents whose IDs are contained in the given list.
     * <p>
     * Firestore's {@code whereIn} query supports up to 10 document IDs per call,
     * so this method splits the input into chunks of at most 10 IDs and issues
     * multiple queries. Once all queries succeed, the results are merged into
     * a single list of {@link User} objects containing the user ID and name.
     *
     * @param ids      list of user document IDs to fetch
     * @param callback callback to receive the resulting users or an error
     */
    public void fetchUsersByIds(@NonNull ArrayList<String> ids, @NonNull UsersCallback callback) {
        if (ids.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        ArrayList<Task<QuerySnapshot>> tasks = new ArrayList<>();

        for (int i = 0; i < ids.size(); i += 10) {
            List<String> chunk = ids.subList(i, Math.min(i + 10, ids.size()));
            Task<QuerySnapshot> t = db.collection("users")
                    .whereIn(FieldPath.documentId(), new ArrayList<>(chunk))
                    .get();
            tasks.add(t);
        }

        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {
                    ArrayList<User> users = new ArrayList<>();

                    for (Object o : results) {
                        QuerySnapshot qs = (QuerySnapshot) o;
                        for (DocumentSnapshot doc : qs){
                            if (!doc.exists()) return;

                            User user = new User();
                            user.setUserId(doc.getId());
                            user.setName(doc.getString("name"));
                            users.add(user);

                        };
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(error -> {
                    callback.onError(error);
                });
    }
}
