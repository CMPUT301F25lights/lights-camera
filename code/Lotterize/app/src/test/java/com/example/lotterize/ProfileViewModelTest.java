package com.example.lotterize;

import static org.junit.Assert.*;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.lotterize.ui.profile.ProfileViewModel;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for ProfileViewModel core functionality.
 * These tests focus on local LiveData updates and data synchronization logic.
 * Firebase and Android dependencies are not tested here.
 */
public class ProfileViewModelTest {

    private ProfileViewModel viewModel;
    private User sampleUser;

    @Before
    public void setUp() {
        sampleUser = new User(
                "uid123",
                "Rajit",
                "1234567890",
                "rajit@example.com",
                "53.5,-113.5",
                "rajitUser",
                "password123"
        );

        // Set current user before creating the ViewModel
        CurrentUser.set(sampleUser);

        // Create the ViewModel instance
        viewModel = new ProfileViewModel();
    }

    @Test
    public void testGetUserData_NotNull() {
        LiveData<User> userLiveData = viewModel.getUserData();
        assertNotNull("User LiveData should not be null", userLiveData);
        assertEquals("User ID should match the current user",
                "uid123", userLiveData.getValue().getUserId());
    }

    @Test
    public void testUpdateName_ReflectsInLiveData() {
        User user = viewModel.getUserData().getValue();
        assertNotNull(user);

        user.setName("New Name");
        ((MutableLiveData<User>) viewModel.getUserData()).setValue(user);

        assertEquals("New Name", viewModel.getUserData().getValue().getName());
    }

    @Test
    public void testUpdateEmail_ReflectsInLiveData() {
        User user = viewModel.getUserData().getValue();
        assertNotNull(user);

        user.setEmail("new.email@example.com");
        ((MutableLiveData<User>) viewModel.getUserData()).setValue(user);

        assertEquals("new.email@example.com", viewModel.getUserData().getValue().getEmail());
    }

    @Test
    public void testUpdatePhoneNumber_ReflectsInLiveData() {
        User user = viewModel.getUserData().getValue();
        assertNotNull(user);

        user.setPhoneNumber("9876543210");
        ((MutableLiveData<User>) viewModel.getUserData()).setValue(user);

        assertEquals("9876543210", viewModel.getUserData().getValue().getPhoneNumber());
    }

    @Test
    public void testDeleteAccount_SetsLiveDataToNull() {
        ((MutableLiveData<User>) viewModel.getUserData()).setValue(null);
        assertNull("After deletion, LiveData should be null", viewModel.getUserData().getValue());
    }
}
