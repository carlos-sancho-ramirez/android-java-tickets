package sword.tickets.android.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;

import androidx.annotation.NonNull;

import sword.tickets.android.DbManager;
import sword.tickets.android.activities.ReleaseTypePickerActivity;
import sword.tickets.android.db.ProjectId;
import sword.tickets.android.db.ProjectIdParceler;
import sword.tickets.android.db.TicketsDbSchema;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;

public final class CreateReleaseReleaseTypePickerController implements ReleaseTypePickerActivity.Controller {

    private static final int REQUEST_CODE_NEXT_STEP = 1;

    @NonNull
    private final ProjectId _projectId;

    CreateReleaseReleaseTypePickerController(@NonNull ProjectId projectId) {
        ensureNonNull(projectId);
        _projectId = projectId;
    }

    void fire(@NonNull Activity activity, int requestCode) {
        if (DbManager.getInstance().getManager().hasAtLeastOneRelease()) {
            ReleaseTypePickerActivity.open(activity, requestCode, this);
        }
        else {
            new CreateReleaseReleaseAnchorPickerController(_projectId, TicketsDbSchema.ReleaseType.MAJOR).fire(activity, requestCode);
        }
    }

    @Override
    public void onActivityResult(@NonNull Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_NEXT_STEP && resultCode == Activity.RESULT_OK) {
            activity.setResult(Activity.RESULT_OK, data);
            activity.finish();
        }
    }

    @Override
    public void pickReleaseType(@NonNull Activity activity, @NonNull TicketsDbSchema.ReleaseType releaseType) {
        new CreateReleaseReleaseAnchorPickerController(_projectId, releaseType).fire(activity, REQUEST_CODE_NEXT_STEP);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        ProjectIdParceler.write(dest, _projectId);
    }

    public static final Creator<CreateReleaseReleaseTypePickerController> CREATOR = new Creator<CreateReleaseReleaseTypePickerController>() {

        @NonNull
        @Override
        public CreateReleaseReleaseTypePickerController createFromParcel(Parcel source) {
            final ProjectId projectId = ProjectIdParceler.read(source);
            return new CreateReleaseReleaseTypePickerController(projectId);
        }

        @NonNull
        @Override
        public CreateReleaseReleaseTypePickerController[] newArray(int size) {
            return new CreateReleaseReleaseTypePickerController[size];
        }
    };
}
