package sword.tickets.android.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;

import androidx.annotation.NonNull;

import sword.tickets.android.DbManager;
import sword.tickets.android.Intentions.ResultKeys;
import sword.tickets.android.activities.NewTicketActivity;
import sword.tickets.android.db.ProjectId;
import sword.tickets.android.db.ProjectIdParceler;
import sword.tickets.android.db.TicketId;
import sword.tickets.android.db.TicketIdBundler;
import sword.tickets.android.db.TicketsDbSchema.TicketType;

public final class CreateTicketForExistingProjectNewTicketController implements NewTicketActivity.Controller {

    @NonNull
    private final ProjectId _projectId;

    CreateTicketForExistingProjectNewTicketController(@NonNull ProjectId projectId) {
        _projectId = projectId;
    }

    @Override
    public void submit(@NonNull Activity activity, String name, String description, @NonNull TicketType type) {
        final TicketId ticketId = DbManager.getInstance().getManager().newTicket(name, description, _projectId, type);

        final Intent data = new Intent();
        TicketIdBundler.writeAsIntentExtra(data, ResultKeys.TICKET_ID, ticketId);
        activity.setResult(Activity.RESULT_OK, data);
        activity.finish();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        ProjectIdParceler.write(dest, _projectId);
    }

    public static final Creator<CreateTicketForExistingProjectNewTicketController> CREATOR = new Creator<CreateTicketForExistingProjectNewTicketController>() {

        @Override
        public CreateTicketForExistingProjectNewTicketController createFromParcel(Parcel source) {
            final ProjectId projectId = ProjectIdParceler.read(source);
            return new CreateTicketForExistingProjectNewTicketController(projectId);
        }

        @Override
        public CreateTicketForExistingProjectNewTicketController[] newArray(int size) {
            return new CreateTicketForExistingProjectNewTicketController[size];
        }
    };
}
