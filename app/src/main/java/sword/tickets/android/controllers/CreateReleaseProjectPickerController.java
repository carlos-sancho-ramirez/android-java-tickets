package sword.tickets.android.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;

import androidx.annotation.NonNull;

import sword.collections.ImmutableMap;
import sword.tickets.android.DbManager;
import sword.tickets.android.activities.ProjectPickerActivity;
import sword.tickets.android.db.ProjectId;

import static sword.tickets.android.PreconditionUtils.ensureValidState;

public final class CreateReleaseProjectPickerController implements ProjectPickerActivity.Controller {

    private static final int REQUEST_CODE_NEXT_STEP = 1;

    public void fire(@NonNull Activity activity, int requestCode) {
        final ImmutableMap<ProjectId, String> projects =  DbManager.getInstance().getManager().getAllProjects();
        ensureValidState(!projects.isEmpty());
        if (projects.size() == 1) {
            new CreateReleaseReleaseTypePickerController(projects.keyAt(0)).fire(activity, REQUEST_CODE_NEXT_STEP);
        }
        else {
            ProjectPickerActivity.open(activity, requestCode, this);
        }
    }

    @Override
    public boolean shouldAllowNewOption() {
        return false;
    }

    @Override
    public void onActivityResult(@NonNull Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_NEXT_STEP && resultCode == Activity.RESULT_OK) {
            activity.setResult(Activity.RESULT_OK, data);
            activity.finish();
        }
    }

    @Override
    public void pickProject(@NonNull Activity activity, @NonNull ProjectId projectId) {
        new CreateReleaseReleaseTypePickerController(projectId).fire(activity, REQUEST_CODE_NEXT_STEP);
    }

    @Override
    public void newProject(@NonNull Activity activity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        // Nothing to add
    }

    public static final Creator<CreateReleaseProjectPickerController> CREATOR = new Creator<CreateReleaseProjectPickerController>() {

        @Override
        public CreateReleaseProjectPickerController createFromParcel(Parcel source) {
            return new CreateReleaseProjectPickerController();
        }

        @Override
        public CreateReleaseProjectPickerController[] newArray(int size) {
            return new CreateReleaseProjectPickerController[size];
        }
    };
}
