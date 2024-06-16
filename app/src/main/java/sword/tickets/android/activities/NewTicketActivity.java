package sword.tickets.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import sword.tickets.android.DbManager;
import sword.tickets.android.R;

public final class NewTicketActivity extends android.app.Activity {

    public static void open(@NonNull Activity activity, int requestCode) {
        final Intent intent = new Intent(activity, NewTicketActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_ticket);
        final EditText nameField = findViewById(R.id.ticketNameField);
        final EditText descriptionField = findViewById(R.id.ticketDescriptionField);
        findViewById(R.id.submitButton).setOnClickListener(v -> {
            final String name = nameField.getText().toString();
            final String description = descriptionField.getText().toString();
            DbManager.getInstance().getManager().newTicket(name, description);
            setResult(Activity.RESULT_OK);
            finish();
        });
    }
}
