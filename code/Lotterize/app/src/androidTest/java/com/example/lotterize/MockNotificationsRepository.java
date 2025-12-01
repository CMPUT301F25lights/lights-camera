package com.example.lotterize;

import androidx.annotation.NonNull;

import com.example.lotterize.NotificationsRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory mock for NotificationsRepository.
 * Tests can push fake notifications into internal maps.
 */
public class MockNotificationsRepository extends NotificationsRepository {

    // Map<userId, notifications they received>
    private final Map<String, ArrayList<Notification>> receivedMap = new HashMap<>();

    // Map<userId, notifications they sent>
    private final Map<String, ArrayList<Notification>> sentMap = new HashMap<>();

    public void addReceived(@NonNull String userId, @NonNull Notification notification) {
        receivedMap
                .computeIfAbsent(userId, k -> new ArrayList<>())
                .add(notification);
    }

    public void addSent(@NonNull String userId, @NonNull Notification notification) {
        sentMap
                .computeIfAbsent(userId, k -> new ArrayList<>())
                .add(notification);
    }

    @Override
    public void fetchNotificationsReceived(@NonNull String userId,
                                           @NonNull NotificationsCallback callback) {
        ArrayList<Notification> list =
                receivedMap.getOrDefault(userId, new ArrayList<>());
        callback.onSuccess(new ArrayList<>(list));
    }

    @Override
    public void fetchNotificationsSent(@NonNull String userId,
                                       @NonNull NotificationsCallback callback) {
        ArrayList<Notification> list =
                sentMap.getOrDefault(userId, new ArrayList<>());
        callback.onSuccess(new ArrayList<>(list));
    }
}
