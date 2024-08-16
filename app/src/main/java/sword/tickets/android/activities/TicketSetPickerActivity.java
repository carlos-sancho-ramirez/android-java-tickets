package sword.tickets.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import sword.collections.ImmutableSet;
import sword.collections.Set;
import sword.tickets.android.DbManager;
import sword.tickets.android.db.ProjectId;
import sword.tickets.android.db.TicketId;
import sword.tickets.android.layout.TicketSetPickerEntryLayout;
import sword.tickets.android.layout.TicketSetPickerLayoutForActivity;
import sword.tickets.android.models.TicketReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidState;

public final class TicketSetPickerActivity extends Activity {

    private interface ArgKeys {
        String CONTROLLER = "controller";
    }

    public static void open(@NonNull Activity activity, int requestCode, @NonNull Controller controller) {
        ensureNonNull(controller);
        final Intent intent = new Intent(activity, TicketSetPickerActivity.class);
        intent.putExtra(ArgKeys.CONTROLLER, controller);
        activity.startActivityForResult(intent, requestCode);
    }

    private ImmutableSet<TicketReference<TicketId>> _tickets;

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
        _tickets = DbManager.getInstance().getManager().getAllCompletedTicketsForProject(getController().getProjectId());

        for (TicketReference<TicketId> ticket : _tickets) {
            TicketSetPickerEntryLayout.attach(layout.ticketsContainer()).view().setText(ticket.name);
        }
    }

    public interface Controller extends Parcelable {
        ProjectId getProjectId();
        void onActivityResult(@NonNull Activity activity, int requestCode, int resultCode, Intent data);
        void pickTicketSet(@NonNull Activity activity, @NonNull Set<TicketId> tickets);
    }
}
