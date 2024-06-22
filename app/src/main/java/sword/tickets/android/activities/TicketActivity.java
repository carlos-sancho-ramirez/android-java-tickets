package sword.tickets.android.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import sword.tickets.android.DbManager;
import sword.tickets.android.R;
import sword.tickets.android.db.TicketId;
import sword.tickets.android.db.TicketIdBundler;
import sword.tickets.android.db.models.Ticket;
import sword.tickets.android.layout.TicketLayoutForActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidState;

public final class TicketActivity extends Activity {

    private static final int REQUEST_CODE_EDIT_TICKET = 1;

    private interface ArgKeys {
        String TICKET_ID = "ticketId";
    }

    static void open(@NonNull Context context, @NonNull TicketId ticketId) {
        ensureNonNull(ticketId);

        final Intent intent = new Intent(context, TicketActivity.class);
        TicketIdBundler.writeAsIntentExtra(intent, ArgKeys.TICKET_ID, ticketId);
        context.startActivity(intent);
    }

    @NonNull
    private TicketId getTicketId() {
        final TicketId id = TicketIdBundler.readAsIntentExtra(getIntent(), ArgKeys.TICKET_ID);
        ensureValidState(id != null);
        return id;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final TicketLayoutForActivity layout = TicketLayoutForActivity.attach(this);

        final Ticket ticket = DbManager.getInstance().getManager().getTicket(getTicketId());
        if (ticket == null) {
            layout.ticketNotFoundErrorTextView().setVisibility(View.VISIBLE);
        }
        else {
            layout.ticketNameField().setText(ticket.name);
            layout.ticketDescriptionField().setText(ticket.description);
            layout.infoPanel().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.ticket, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.optionEdit) {
            EditTicketActivity.open(this, REQUEST_CODE_EDIT_TICKET, getTicketId());
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }
}
