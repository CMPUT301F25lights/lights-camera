package com.example.lotterize.ui.profile;

/**
 * Callback interface used to handle field updates from dialogs.
 */
public interface OnValueChanged {

    /**
     * Called when the user saves a new valid value.
     *
     * @param newValue The updated value entered by the user.
     */
    void onChanged(String newValue);

}
