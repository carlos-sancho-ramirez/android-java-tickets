package sword.tickets.android;

import android.app.Activity;

import sword.tickets.android.controllers.CreateReleaseTicketSetPickerController;
import sword.tickets.android.controllers.CreateTicketProjectPickerController;
import sword.tickets.android.db.ProjectId;

import androidx.annotation.NonNull;

public final class Intentions {

    public interface ResultKeys {
        String TICKET_ID = "ticketId";
    }

    /**
     * Starts the process of creating a new ticket.
     * <p>
     * If case of success, onActivityResult will be triggered in the given activity with the given
     * requestCode, {@link Activity#RESULT_OK} as resultCode and the {@link ResultKeys#TICKET_ID}
     * filled with the identifier of the new created ticket in the data.
     *
     * @param activity Activity that will receive the call to onActivityResult once the process is finished.
     * @param requestCode Code that will be given in the onActivityResult callback.
     */
    public static void createTicket(@NonNull Activity activity, int requestCode) {
        new CreateTicketProjectPickerController().fire(activity, requestCode);
    }

    public static void createRelease(@NonNull Activity activity, int requestCode) {
        // TODO: The project picker must be open first, instead of hardcoding the first project
        final ProjectId projectId = DbManager.getInstance().getManager().getAllProjects().keyAt(0);
        new CreateReleaseTicketSetPickerController(projectId).fire(activity, requestCode);
    }

    private Intentions() {
    }
}
