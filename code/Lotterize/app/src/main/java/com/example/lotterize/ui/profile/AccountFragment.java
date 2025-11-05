package com.example.lotterize.ui.profile;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lotterize.R;
import com.example.lotterize.databinding.FragmentAccountBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Fragment that allows users to view and edit their account information
 * such as name, email, and phone number.
 * When the user taps on a field, a dialog appears allowing them to update
 * that specific detail. The updated values are reflected both on-screen and
 * in the ProfileViewModel to maintain data consistency.
 */
public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private ProfileViewModel profileViewModel;

    /**
     * Inflates the fragment layout and initializes the profile data observers
     * and click listeners for the editable fields.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ViewModel provides user data and handles updates
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        // Observe and update UI whenever user data changes
        profileViewModel.getUserData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.textNameValue.setText(user.getName());
                binding.textEmailValue.setText(user.getEmail());
                binding.textPhoneValue.setText(user.getPhoneNumber());
            }
        });

        // Hide bottom navigation bar while editing account info
        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
            if (navView != null) {
                navView.setVisibility(View.GONE);
            }
        }

        // Back button navigation
        binding.buttonBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });

        // Handle edit actions for each field
        setupEditableFields();

        return root;
    }

    /**
     * Sets click listeners on each profile field (Name, Email, Phone)
     * to open an edit dialog when tapped.
     */
    private void setupEditableFields() {
        // Edit Name
        binding.layoutName.setOnClickListener(v -> {
            showEditDialog("Edit Name", "Name", binding.textNameValue.getText().toString(),
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    newValue -> {
                        binding.textNameValue.setText(newValue);
                        profileViewModel.updateName(newValue);
                    });
        });

        // Edit Email
        binding.layoutEmail.setOnClickListener(v -> {
            showEditDialog("Edit Email", "Email", binding.textEmailValue.getText().toString(),
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                    newValue -> {
                        binding.textEmailValue.setText(newValue);
                        profileViewModel.updateEmail(newValue);
                    });
        });

        // Edit Phone Number
        binding.layoutPhone.setOnClickListener(v -> {
            showEditDialog("Edit Phone Number", "Phone Number", binding.textPhoneValue.getText().toString(),
                    InputType.TYPE_CLASS_PHONE,
                    newValue -> {
                        binding.textPhoneValue.setText(newValue);
                        profileViewModel.updatePhoneNumber(newValue);
                    });
        });
    }

    /**
     * Displays a reusable dialog allowing the user to edit a text field.
     * The dialog validates the input and triggers a callback when saved.
     *
     * @param title The dialog title.
     * @param hint The text field hint (label).
     * @param currentValue The existing value to pre-fill the input.
     * @param inputType The input type (e.g., text, phone, email).
     * @param callback The callback triggered when a valid new value is saved.
     */
    private void showEditDialog(String title, String hint, String currentValue, int inputType, OnValueChanged callback) {
        if (getContext() == null) return;

        // Initialize dialog
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_field);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Initialize views
        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        TextInputLayout textInputLayout = dialog.findViewById(R.id.text_input_layout);
        TextInputEditText editText = dialog.findViewById(R.id.edit_text_field);
        Button buttonCancel = dialog.findViewById(R.id.button_cancel);
        Button buttonSave = dialog.findViewById(R.id.button_save);

        // Populate dialog UI
        dialogTitle.setText(title);
        textInputLayout.setHint(hint);
        editText.setText(currentValue);
        editText.setInputType(inputType);
        editText.setSelection(currentValue.length());

        // Cancel action
        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        // Save action
        buttonSave.setOnClickListener(v -> {
            String newValue = editText.getText().toString().trim();
            if (newValue.isEmpty()) {
                textInputLayout.setError(hint + " cannot be empty");
                return;
            }

            // Validate email format
            if (inputType == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                    && !android.util.Patterns.EMAIL_ADDRESS.matcher(newValue).matches()) {
                textInputLayout.setError("Please enter a valid email");
                return;
            }

            // Validate phone number length
            if (inputType == InputType.TYPE_CLASS_PHONE && newValue.length() < 10) {
                textInputLayout.setError("Please enter a valid phone number");
                return;
            }

            // Pass value to callback
            callback.onChanged(newValue);
            Toast.makeText(getContext(), title + " updated successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Restores navigation visibility when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
