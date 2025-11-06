package com.example.lotterize;
import org.junit.Test;
import java.util.*;

import static org.junit.Assert.*;
import static com.example.lotterize.ui.addEvents.EntrantListFragment.*;

import com.example.lotterize.ui.addEvents.EntrantListFragment;

public class EntrantListTest {

    @Test
    public void applyNamesTest() {
        ArrayList<String> ids = new ArrayList<>(Arrays.asList("UserId1","UserId2","UserId3"));
        Map<String,String> map = new HashMap<>();

        map.put("UserId1","Nathan");
        map.put("UserId3","");

        ArrayList<String> userNames = EntrantListFragment.applyNames(ids, map);
        assertEquals(Arrays.asList("Nathan","UserId2","UserId3"), userNames);
    }
}
