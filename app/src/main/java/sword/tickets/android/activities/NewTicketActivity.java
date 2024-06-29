package sword.tickets.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import sword.tickets.android.layout.NewTicketLayoutForActivity;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidState;

public final class NewTicketActivity extends android.app.Activity {

    private interface ArgKeys {
        String CONTROLLER = "controller";
    }

    public static void open(@NonNull Activity activity, int requestCode, @NonNull Controller controller) {
        ensureNonNull(controller);
        final Intent intent = new Intent(activity, NewTicketActivity.class);
        intent.putExtra(ArgKeys.CONTROLLER, controller);
        activity.startActivityForResult(intent, requestCode);
    }

    @NonNull
    private Controller getController() {
        final Controller controller = getIntent().getParcelableExtra(ArgKeys.CONTROLLER, Controller.class);
        ensureValidState(controller != null);
        return controller;
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
            getController().submit(this, name, description);
        });
    }

    public interface Controller extends Parcelable {
        void submit(@NonNull Activity activity, String name, String description);
    }
}
