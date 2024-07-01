package sword.tickets.android.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import sword.tickets.android.DbManager;
import sword.tickets.android.Intentions.ResultKeys;
import sword.tickets.android.activities.NewTicketActivity;
import sword.tickets.android.db.ProjectId;
import sword.tickets.android.db.TicketId;
import sword.tickets.android.db.TicketIdBundler;
import sword.tickets.android.db.TicketsDbManagerImpl;
import sword.tickets.android.db.TicketsDbSchema;

public final class CreateTicketForNewProjectNewTicketController implements NewTicketActivity.Controller {

    private final String _projectName;

    CreateTicketForNewProjectNewTicketController(String projectName) {
        _projectName = projectName;
    }

    @Override
    public void submit(@NonNull Activity activity, String name, String description, @NonNull TicketsDbSchema.TicketType type) {
        final TicketsDbManagerImpl manager = DbManager.getInstance().getManager();
        final ProjectId projectId = manager.newProject(_projectName);
        final TicketId ticketId = manager.newTicket(name, description, projectId, type);

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
        dest.writeString(_projectName);
    }

    public static final Parcelable.Creator<CreateTicketForNewProjectNewTicketController> CREATOR = new Parcelable.Creator<CreateTicketForNewProjectNewTicketController>() {

        @Override
        public CreateTicketForNewProjectNewTicketController createFromParcel(Parcel source) {
            final String projectName = source.readString();
            return new CreateTicketForNewProjectNewTicketController(projectName);
        }

        @Override
        public CreateTicketForNewProjectNewTicketController[] newArray(int size) {
            return new CreateTicketForNewProjectNewTicketController[size];
        }
    };
}
