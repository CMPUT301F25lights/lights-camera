package com.example.lotterize;

import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;

import com.example.lotterize.ui.addEvents.EventsRepository;
import com.example.lotterize.ui.addEvents.UsersRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MockUsersRepository extends UsersRepository {
    private final Map<String, String> nameMap = new HashMap<>();

    public void setName(String userId, String displayName) {
        nameMap.put(userId, displayName);
    }


    @Override
    public void resolveDisplayNames(@NonNull ArrayList<String> docIds, @NonNull NamesCallback callback) {
        ArrayList<String> result = new ArrayList<>();
        for (String id : docIds) {
            String name = nameMap.get(id);
            result.add(name != null ? name : id);
        }

        callback.onSuccess(result);
    }

    @Override
    public void fetchUsersByIds(@NonNull ArrayList<String> ids, @NonNull UsersCallback callback) {
        ArrayList<User> users = new ArrayList<>();
        for (String id : ids) {
            User u = new User();
            u.setUserId(id);
            u.setName(nameMap.get(id));
            users.add(u);
        }
        callback.onSuccess(users);
    }
}
