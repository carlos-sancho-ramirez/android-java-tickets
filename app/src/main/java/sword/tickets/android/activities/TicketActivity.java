package sword.tickets.android.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import sword.tickets.android.DbManager;
import sword.tickets.android.R;
import sword.tickets.android.db.TicketId;
import sword.tickets.android.db.TicketIdBundler;
import sword.tickets.android.db.models.Ticket;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidState;

public final class TicketActivity extends Activity {

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
        setContentView(R.layout.ticket);

        final Ticket ticket = DbManager.getInstance().getManager().getTicket(getTicketId());
        if (ticket == null) {
            findViewById(R.id.ticketNotFoundErrorTextView).setVisibility(View.VISIBLE);
        }
        else {
            this.<TextView>findViewById(R.id.ticketNameField).setText(ticket.name);
            this.<TextView>findViewById(R.id.ticketDescriptionField).setText(ticket.description);
            findViewById(R.id.infoPanel).setVisibility(View.VISIBLE);
        }
    }
}
