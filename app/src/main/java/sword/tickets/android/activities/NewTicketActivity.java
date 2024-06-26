package sword.tickets.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import sword.tickets.android.DbManager;
import sword.tickets.android.layout.NewTicketLayoutForActivity;

public final class NewTicketActivity extends android.app.Activity {

    public static void open(@NonNull Activity activity, int requestCode) {
        final Intent intent = new Intent(activity, NewTicketActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final NewTicketLayoutForActivity layout = NewTicketLayoutForActivity.attach(this);
        final EditText nameField = layout.ticketNameField();
        final EditText descriptionField = layout.ticketDescriptionField();
        layout.submitButton().setOnClickListener(v -> {
            final String name = nameField.getText().toString();
            final String description = descriptionField.getText().toString();
            DbManager.getInstance().getManager().newTicket(name, description);
            setResult(Activity.RESULT_OK);
            finish();
        });
    }
}
