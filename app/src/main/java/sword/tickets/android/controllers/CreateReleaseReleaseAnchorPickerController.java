package sword.tickets.android.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;

import androidx.annotation.NonNull;

import sword.collections.ImmutableMap;
import sword.tickets.android.DbManager;
import sword.tickets.android.activities.ReleaseAnchorPickerActivity;
import sword.tickets.android.db.ProjectId;
import sword.tickets.android.db.ProjectIdParceler;
import sword.tickets.android.db.ReleaseId;
import sword.tickets.android.db.ReleaseTypeParceler;
import sword.tickets.android.db.TicketsDbSchema.ReleaseType;
import sword.tickets.android.models.Release;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;

public final class CreateReleaseReleaseAnchorPickerController implements ReleaseAnchorPickerActivity.Controller {

    private static final int REQUEST_CODE_NEXT_STEP = 1;

    @NonNull
    private final ProjectId _projectId;

    @NonNull
    private final ReleaseType _releaseType;

    CreateReleaseReleaseAnchorPickerController(
            @NonNull ProjectId projectId,
            @NonNull ReleaseType releaseType) {
        ensureNonNull(projectId, releaseType);
        _projectId = projectId;
        _releaseType = releaseType;
    }

    public void fire(@NonNull Activity activity, int requestCode) {
        final ImmutableMap<ReleaseId, Release> releases = DbManager.getInstance().getManager().getAllReleasesForProject(_projectId);
        if (releases.size() == 1) {
            new CreateReleaseTicketSetPickerController(_projectId, _releaseType, releases.keyAt(0)).fire(activity, requestCode);
        }
        else if (_releaseType == ReleaseType.MAJOR) {
            final ImmutableMap<ReleaseId, Release> majorReleases = releases.filter(r -> r.minor == 0 && r.bugFix == 0);
            final ReleaseId anchor;
            if (majorReleases.isEmpty()) {
                anchor = null;
            }
            else {
                anchor = majorReleases.filter(r -> majorReleases.allMatch(rr -> r.major >= rr.major)).keyAt(0);
            }

            new CreateReleaseTicketSetPickerController(_projectId, ReleaseType.MAJOR, anchor).fire(activity, requestCode);
        }
        else if (_releaseType == ReleaseType.MINOR) {
            if (releases.toList().groupByInt(r -> r.major).size() == 1) {
                final int mapSize = releases.size();
                ReleaseId anchor = releases.keyAt(0);
                int anchorMinor = releases.valueAt(0).minor;
                for (int i = 1; i < mapSize; i++) {
                    if (releases.valueAt(i).minor > anchorMinor) {
                        anchor = releases.keyAt(i);
                        anchorMinor = releases.valueAt(i).minor;
                    }
                }

                new CreateReleaseTicketSetPickerController(_projectId, ReleaseType.MINOR, anchor).fire(activity, requestCode);
            }
            else {
                ReleaseAnchorPickerActivity.open(activity, requestCode, this);
            }
        }
        else {
            ReleaseAnchorPickerActivity.open(activity, requestCode, this);
        }
    }

    @NonNull
    @Override
    public ProjectId getProjectId() {
        return _projectId;
    }

    @NonNull
    @Override
    public ReleaseType getReleaseType() {
        return _releaseType;
    }

    @Override
    public void onActivityResult(@NonNull Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_NEXT_STEP && resultCode == Activity.RESULT_OK) {
            activity.setResult(Activity.RESULT_OK, data);
            activity.finish();
        }
    }

    @Override
    public void pickAnchor(@NonNull Activity activity, @NonNull ReleaseId releaseId) {
        new CreateReleaseTicketSetPickerController(_projectId, _releaseType, releaseId).fire(activity, REQUEST_CODE_NEXT_STEP);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        ProjectIdParceler.write(dest, _projectId);
        ReleaseTypeParceler.write(dest, _releaseType);
    }

    public static final Creator<CreateReleaseReleaseAnchorPickerController> CREATOR = new Creator<CreateReleaseReleaseAnchorPickerController>() {

        @NonNull
        @Override
        public CreateReleaseReleaseAnchorPickerController createFromParcel(Parcel source) {
            final ProjectId projectId = ProjectIdParceler.read(source);
            final ReleaseType releaseType = ReleaseTypeParceler.read(source);
            return new CreateReleaseReleaseAnchorPickerController(projectId, releaseType);
        }

        @NonNull
        @Override
        public CreateReleaseReleaseAnchorPickerController[] newArray(int size) {
            return new CreateReleaseReleaseAnchorPickerController[size];
        }
    };
}
