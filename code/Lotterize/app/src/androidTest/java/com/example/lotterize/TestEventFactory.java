package com.example.lotterize;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;

public class TestEventFactory {

    public static Event sample(String id, String name, String location) {
        return new Event(
                id,
                "owner1",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                name,
                new Timestamp(new Date()),
                new Timestamp(new Date()),
                new Timestamp(new Date()),
                location,
                50L,
                "desc",
                10L,
                "QR123",
                null,
                null,
                new ArrayList<>(),
                false,
                null
        );
    }
}