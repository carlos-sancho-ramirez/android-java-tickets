package sword.tickets.android.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import sword.tickets.android.DbManager;
import sword.tickets.android.R;
import sword.tickets.android.db.ProjectId;
import sword.tickets.android.db.TicketId;
import sword.tickets.android.db.TicketIdBundler;
import sword.tickets.android.layout.TicketLayoutForActivity;
import sword.tickets.android.models.Ticket;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidState;
import static sword.tickets.android.list.adapters.TicketStatePickerAdapter.STATE_TEXTS;

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

    private TicketLayoutForActivity _layout;
    private boolean _uiJustUpdated;

    @NonNull
    private TicketId getTicketId() {
        final TicketId id = TicketIdBundler.readAsIntentExtra(getIntent(), ArgKeys.TICKET_ID);
        ensureValidState(id != null);
        return id;
    }

    private void updateModelAndUi() {
        final Ticket<ProjectId> ticket = DbManager.getInstance().getManager().getTicket(getTicketId());
        if (ticket == null) {
            _layout.ticketNotFoundErrorTextView().setVisibility(View.VISIBLE);
        }
        else {
            _layout.ticketNameField().setText(ticket.name);
            _layout.ticketStateField().setText(STATE_TEXTS.get(ticket.state));
            _layout.ticketDescriptionField().setText(ticket.description);
            _layout.infoPanel().setVisibility(View.VISIBLE);
            _uiJustUpdated = true;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _layout = TicketLayoutForActivity.attach(this);
        updateModelAndUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!_uiJustUpdated) {
            updateModelAndUi();
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

    @Override
    protected void onPause() {
        _uiJustUpdated = false;
        super.onPause();
    }
}
