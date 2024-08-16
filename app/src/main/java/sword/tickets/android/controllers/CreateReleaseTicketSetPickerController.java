package sword.tickets.android.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.widget.Toast;

import sword.collections.Set;
import sword.tickets.android.activities.TicketSetPickerActivity;
import sword.tickets.android.db.ProjectId;
import sword.tickets.android.db.ProjectIdParceler;
import sword.tickets.android.db.TicketId;

import androidx.annotation.NonNull;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;

public final class CreateReleaseTicketSetPickerController implements TicketSetPickerActivity.Controller {

    private static final int REQUEST_CODE_NEXT_STEP = 1;

    @NonNull
    private final ProjectId _projectId;

    public CreateReleaseTicketSetPickerController(@NonNull ProjectId projectId) {
        ensureNonNull(projectId);
        _projectId = projectId;
    }

    public void fire(@NonNull Activity activity, int requestCode) {
        TicketSetPickerActivity.open(activity, requestCode, this);
    }

    @Override
    public ProjectId getProjectId() {
        return _projectId;
    }

    @Override
    public void onActivityResult(@NonNull Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_NEXT_STEP && resultCode == Activity.RESULT_OK) {
            activity.setResult(Activity.RESULT_OK, data);
            activity.finish();
        }
    }

    @Override
    public void pickTicketSet(@NonNull Activity activity, @NonNull Set<TicketId> tickets) {
        Toast.makeText(activity, "To be implemented", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        ProjectIdParceler.write(dest, _projectId);
    }

    public static final Creator<CreateReleaseTicketSetPickerController> CREATOR = new Creator<CreateReleaseTicketSetPickerController>() {

        @Override
        public CreateReleaseTicketSetPickerController createFromParcel(Parcel source) {
            final ProjectId projectId = ProjectIdParceler.read(source);
            return new CreateReleaseTicketSetPickerController(projectId);
        }

        @Override
        public CreateReleaseTicketSetPickerController[] newArray(int size) {
            return new CreateReleaseTicketSetPickerController[size];
        }
    };
}
