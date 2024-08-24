package sword.tickets.android.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;

import androidx.annotation.NonNull;

import sword.collections.Set;
import sword.tickets.android.DbManager;
import sword.tickets.android.Intentions;
import sword.tickets.android.activities.TicketSetPickerActivity;
import sword.tickets.android.db.ProjectId;
import sword.tickets.android.db.ProjectIdParceler;
import sword.tickets.android.db.ReleaseId;
import sword.tickets.android.db.ReleaseIdBundler;
import sword.tickets.android.db.ReleaseIdParceler;
import sword.tickets.android.db.ReleaseTypeParceler;
import sword.tickets.android.db.TicketId;
import sword.tickets.android.db.TicketsDbManagerImpl;
import sword.tickets.android.db.TicketsDbSchema;
import sword.tickets.android.models.Release;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidArguments;

public final class CreateReleaseTicketSetPickerController implements TicketSetPickerActivity.Controller {

    @NonNull
    private final ProjectId _projectId;

    @NonNull
    private final TicketsDbSchema.ReleaseType _releaseType;
    private final ReleaseId _anchor;

    public CreateReleaseTicketSetPickerController(@NonNull ProjectId projectId, @NonNull TicketsDbSchema.ReleaseType releaseType, ReleaseId anchor) {
        ensureNonNull(projectId, releaseType);
        ensureValidArguments(releaseType == TicketsDbSchema.ReleaseType.MAJOR || anchor != null);
        _projectId = projectId;
        _releaseType = releaseType;
        _anchor = anchor;
    }

    @NonNull
    private ReleaseId createReleaseInDatabase() {
        final TicketsDbManagerImpl manager = DbManager.getInstance().getManager();
        int major = 1;
        int minor = 0;
        int bugFix = 0;
        if (_anchor != null) {
            final Release anchor = manager.getRelease(_anchor);
            if (_releaseType == TicketsDbSchema.ReleaseType.MAJOR) {
                major = anchor.major + 1;
            }
            else if (_releaseType == TicketsDbSchema.ReleaseType.MINOR) {
                major = anchor.major;
                minor = anchor.minor + 1;
            }
            else {
                major = anchor.major;
                minor = anchor.minor;
                bugFix = anchor.bugFix + 1;
            }
        }

        return manager.newRelease(_projectId, major, minor, bugFix);
    }

    private void finishActivityWithResult(@NonNull Activity activity, @NonNull ReleaseId releaseId) {
        final Intent intent = new Intent();
        ReleaseIdBundler.writeAsIntentExtra(intent, Intentions.ResultKeys.RELEASE_ID, releaseId);
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    public void fire(@NonNull Activity activity, int requestCode) {
        if (DbManager.getInstance().getManager().hasAtLeastOneTicketWithoutReleaseForProject(_projectId)) {
            TicketSetPickerActivity.open(activity, requestCode, this);
        }
        else {
            final ReleaseId releaseId = createReleaseInDatabase();
            finishActivityWithResult(activity, releaseId);
        }
    }

    @Override
    public ProjectId getProjectId() {
        return _projectId;
    }

    @Override
    public void pickTicketSet(@NonNull Activity activity, @NonNull Set<TicketId> tickets) {
        final ReleaseId releaseId = createReleaseInDatabase();

        final TicketsDbManagerImpl manager = DbManager.getInstance().getManager();
        for (TicketId ticketId : tickets) {
            manager.updateTicketRelease(ticketId, releaseId);
        }

        finishActivityWithResult(activity, releaseId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        ProjectIdParceler.write(dest, _projectId);
        ReleaseTypeParceler.write(dest, _releaseType);
        ReleaseIdParceler.write(dest, _anchor);
    }

    public static final Creator<CreateReleaseTicketSetPickerController> CREATOR = new Creator<CreateReleaseTicketSetPickerController>() {

        @Override
        public CreateReleaseTicketSetPickerController createFromParcel(Parcel source) {
            final ProjectId projectId = ProjectIdParceler.read(source);
            final TicketsDbSchema.ReleaseType releaseType = ReleaseTypeParceler.read(source);
            final ReleaseId anchor = ReleaseIdParceler.read(source);
            return new CreateReleaseTicketSetPickerController(projectId, releaseType, anchor);
        }

        @Override
        public CreateReleaseTicketSetPickerController[] newArray(int size) {
            return new CreateReleaseTicketSetPickerController[size];
        }
    };
}
