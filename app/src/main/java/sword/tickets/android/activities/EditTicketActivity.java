package sword.tickets.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import sword.tickets.android.DbManager;
import sword.tickets.android.R;
import sword.tickets.android.db.ProjectId;
import sword.tickets.android.db.ReleaseId;
import sword.tickets.android.db.TicketId;
import sword.tickets.android.db.TicketIdBundler;
import sword.tickets.android.db.TicketsDbSchema;
import sword.tickets.android.layout.EditTicketLayoutForActivity;
import sword.tickets.android.list.adapters.TicketStatePickerAdapter;
import sword.tickets.android.models.Ticket;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidState;

public final class EditTicketActivity extends Activity {

    private interface ArgKeys {
        String TICKET_ID = "ticketId";
    }

    public static void open(@NonNull Activity activity, int requestCode, @NonNull TicketId ticketId) {
        ensureNonNull(ticketId);

        final Intent intent = new Intent(activity, EditTicketActivity.class);
        TicketIdBundler.writeAsIntentExtra(intent, ArgKeys.TICKET_ID, ticketId);
        activity.startActivityForResult(intent, requestCode);
    }

    @NonNull
    private TicketId getTicketId() {
        final TicketId value = TicketIdBundler.readAsIntentExtra(getIntent(), ArgKeys.TICKET_ID);
        ensureValidState(value != null);
        return value;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final EditTicketLayoutForActivity layout = EditTicketLayoutForActivity.attach(this);

        final TicketId ticketId = getTicketId();
        final Ticket<ProjectId, ReleaseId> ticket = DbManager.getInstance().getManager().getTicket(ticketId);
        ensureValidState(ticket != null);

        final TicketStatePickerAdapter adapter = new TicketStatePickerAdapter();
        layout.saveButton().setOnClickListener(v -> {
            final String name = layout.ticketNameField().getText().toString();
            final String description = layout.ticketDescriptionField().getText().toString();
            final TicketsDbSchema.TicketState state = adapter.getItem(layout.ticketStateSpinner().getSelectedItemPosition());
            if (DbManager.getInstance().getManager().updateTicket(ticketId, new Ticket<>(name, description, ticket.projectId, ticket.releaseId, state))) {
                setResult(Activity.RESULT_OK);
                finish();
            }
            else {
                Toast.makeText(this, R.string.unableToUpdateTicket, Toast.LENGTH_SHORT).show();
            }
        });

        layout.ticketNameField().setText(ticket.name);
        layout.ticketDescriptionField().setText(ticket.description);

        final Spinner spinner = layout.ticketStateSpinner();
        spinner.setAdapter(adapter);

        final int stateCount = adapter.getCount();
        for (int i = 0; i < stateCount; i++) {
            if (adapter.getItem(i) == ticket.state) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
