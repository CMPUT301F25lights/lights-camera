package com.example.lotterize.ui.notifications;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.lotterize.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.Objects;



/**
 * This is a dialog fragment that lets an organizer compose a notification message.
 */
public class SendNotificationDialogFragment extends DialogFragment {

    // Keys for sending result back
    public static final String RESULT_KEY      = "send_notification_result";
    public static final String RESULT_EVENT_ID = "result_event_id";
    public static final String RESULT_STATUS   = "result_status";
    public static final String RESULT_MESSAGE  = "result_message";

    /**
     * This is the empty public constructor required for fragments.
     */
    public SendNotificationDialogFragment() {}

    /**
     * This creates a new dialog instance and it attaches the required arguments so the dialog can read them later.
     *
     * @param eventId
     *      The id of the event to which this notification relates.
     * @param status
     *      The recipient group/status to send to
     * @return
     *      Returns a {@code SendNotificationDialogFragment} configured with the given arguments.
     */
    public static SendNotificationDialogFragment newInstance(@NonNull String eventId, @NonNull String status) {
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        args.putString("status", status);

        SendNotificationDialogFragment f = new SendNotificationDialogFragment();
        f.setArguments(args);
        return f;
    }

    /**
     * This builds the dialog UI, validates the message input, and sends the result when "Send" is pressed.
     * Responsibilities:
     *    Reads {@code eventId} and {@code status} from arguments. Enables the send button only when the message is non-empty. On send, posts a result bundle using {@link #RESULT_KEY} and dismisses the dialog.
     *
     * @param savedInstanceState
     *      If non-null, the dialog is being re-created from a previous state.
     * @return
     *      Returns a {@link Dialog} containing the composed UI.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String eventId = requireArguments().getString("eventId");
        String status  = requireArguments().getString("status");

        View content = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_send_notification, null, false);

        TextInputLayout tilMessage = content.findViewById(R.id.input_layout_message);
        TextInputEditText editMessage = content.findViewById(R.id.edit_message);
        View btnCancel = content.findViewById(R.id.btn_cancel);
        View btnSend = content.findViewById(R.id.btn_send);

        editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String text = (s == null) ? "" : s.toString().trim();
                if( !text.isEmpty()){
                    btnSend.setEnabled(true);
                    tilMessage.setError(null);
                }
                else{
                    tilMessage.setError("Message cannot be empty");
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        btnCancel.setOnClickListener(v -> dismiss());

        btnSend.setOnClickListener(v -> {
            String msg = editMessage.getText() == null ? "" : editMessage.getText().toString().trim();
            if (msg.isEmpty()) {
                tilMessage.setError("Message cannot be empty");
                return;
            }

            Bundle result = new Bundle();
            result.putString(RESULT_EVENT_ID, eventId);
            result.putString(RESULT_STATUS, status);
            result.putString(RESULT_MESSAGE, msg);
            getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
            dismiss();
        });

        // This creates the dialog with no default positive/negative.
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Send to " + status.toLowerCase())
                .setView(content)
                .create();

    }
}

