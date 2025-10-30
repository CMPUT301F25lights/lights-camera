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

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private ProfileViewModel profileViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        profileViewModel.getUserData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.textNameValue.setText(user.getName());
                binding.textEmailValue.setText(user.getEmail());
                binding.textPhoneValue.setText(user.getPhoneNumber());
            }
        });


        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
            if (navView != null) {
                navView.setVisibility(View.GONE);
            }
        }

        // Back button using Navigation Component
        binding.buttonBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });

        // Name field click
        binding.layoutName.setOnClickListener(v -> {
            showEditDialog("Edit Name", "Name", binding.textNameValue.getText().toString(),
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    newValue -> {
                        binding.textNameValue.setText(newValue);
                        profileViewModel.updateName(newValue);
                    });
        });

        // Email field click
        binding.layoutEmail.setOnClickListener(v -> {
            showEditDialog("Edit Email", "Email", binding.textEmailValue.getText().toString(),
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                    newValue -> {
                        binding.textEmailValue.setText(newValue);
                        profileViewModel.updateEmail(newValue);
                    });
        });

        // Phone field click
        binding.layoutPhone.setOnClickListener(v -> {
            showEditDialog("Edit Phone Number", "Phone Number", binding.textPhoneValue.getText().toString(),
                    InputType.TYPE_CLASS_PHONE,
                    newValue -> {
                        binding.textPhoneValue.setText(newValue);
                        profileViewModel.updatePhoneNumber(newValue);
                    });
        });


        return root;
    }

    private void showEditDialog(String title, String hint, String currentValue, int inputType, OnValueChanged callback) {
        if (getContext() == null) return;

        // Create dialog
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_field);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Get dialog views
        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        TextInputLayout textInputLayout = dialog.findViewById(R.id.text_input_layout);
        TextInputEditText editText = dialog.findViewById(R.id.edit_text_field);
        Button buttonCancel = dialog.findViewById(R.id.button_cancel);
        Button buttonSave = dialog.findViewById(R.id.button_save);

        // Set dialog content
        dialogTitle.setText(title);
        textInputLayout.setHint(hint);
        editText.setText(currentValue);
        editText.setInputType(inputType);
        editText.setSelection(currentValue.length()); // Move cursor to end

        // Cancel button
        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        // Save button
        buttonSave.setOnClickListener(v -> {
            String newValue = editText.getText().toString().trim();
            if (newValue.isEmpty()) {
                textInputLayout.setError(hint + " cannot be empty");
                return;
            }

            // Validate email
            if (inputType == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newValue).matches()) {
                    textInputLayout.setError("Please enter a valid email");
                    return;
                }
            }

            // Validate phone
            if (inputType == InputType.TYPE_CLASS_PHONE) {
                if (newValue.length() < 10) {
                    textInputLayout.setError("Please enter a valid phone number");
                    return;
                }
            }

            callback.onChanged(newValue);
            Toast.makeText(getContext(), title + " updated successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    // Interface for callback
    interface OnValueChanged {
        void onChanged(String newValue);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}