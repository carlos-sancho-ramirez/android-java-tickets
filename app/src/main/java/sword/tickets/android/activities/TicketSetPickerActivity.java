package sword.tickets.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import sword.collections.ImmutableSet;
import sword.collections.MutableHashSet;
import sword.collections.MutableSet;
import sword.collections.Set;
import sword.tickets.android.DbManager;
import sword.tickets.android.db.ProjectId;
import sword.tickets.android.db.TicketId;
import sword.tickets.android.layout.TicketSetPickerEntryLayout;
import sword.tickets.android.layout.TicketSetPickerLayoutForActivity;
import sword.tickets.android.models.TicketReference;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidState;

public final class TicketSetPickerActivity extends Activity {

    private interface ArgKeys {
        String CONTROLLER = "controller";
    }

    private interface SavedKeys {
        String SELECTED_TICKETS = "st";
    }

    public static void open(@NonNull Activity activity, int requestCode, @NonNull Controller controller) {
        ensureNonNull(controller);
        final Intent intent = new Intent(activity, TicketSetPickerActivity.class);
        intent.putExtra(ArgKeys.CONTROLLER, controller);
        activity.startActivityForResult(intent, requestCode);
    }

    private MutableSet<TicketId> _selectedTickets;

    @NonNull
    private Controller getController() {
        final Controller controller = getIntent().getParcelableExtra(ArgKeys.CONTROLLER, Controller.class);
        ensureValidState(controller != null);
        return controller;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final TicketSetPickerLayoutForActivity layout = TicketSetPickerLayoutForActivity.attach(this);
        final Controller controller = getController();
        final ImmutableSet<TicketReference<TicketId>> tickets = DbManager.getInstance().getManager().getAllTicketReferencesWithoutReleaseForProject(controller.getProjectId());

        if (savedInstanceState == null) {
            _selectedTickets = MutableHashSet.empty();
        }
        else {
            final ParcelableTicketIdSet parcelable = savedInstanceState.getParcelable(SavedKeys.SELECTED_TICKETS, ParcelableTicketIdSet.class);
            _selectedTickets = (parcelable != null)? parcelable.get().mutate() : MutableHashSet.empty();
        }

        for (TicketReference<TicketId> ticket : tickets) {
            final CheckBox checkBoxView = TicketSetPickerEntryLayout.attach(layout.ticketsContainer()).view();
            checkBoxView.setText(ticket.name);
            if (_selectedTickets.contains(ticket.id)) {
                checkBoxView.setChecked(true);
            }

            checkBoxView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    _selectedTickets.add(ticket.id);
                }
                else {
                    _selectedTickets.remove(ticket.id);
                }
            });
        }

        layout.finishButton().setOnClickListener(v ->
                controller.pickTicketSet(this, _selectedTickets));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!_selectedTickets.isEmpty()) {
            outState.putParcelable(SavedKeys.SELECTED_TICKETS, new ParcelableTicketIdSet(_selectedTickets.toImmutable()));
        }
    }

    public interface Controller extends Parcelable {
        ProjectId getProjectId();
        void pickTicketSet(@NonNull Activity activity, @NonNull Set<TicketId> tickets);
    }
}
