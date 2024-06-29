package sword.tickets.android.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import sword.tickets.android.DbManager;
import sword.tickets.android.activities.NewProjectActivity;
import sword.tickets.android.activities.NewTicketActivity;
import sword.tickets.android.activities.ProjectPickerActivity;
import sword.tickets.android.db.ProjectId;

public final class CreateTicketProjectPickerController implements ProjectPickerActivity.Controller {

    private static final int REQUEST_CODE_NEXT_STEP = 1;

    public void fire(@NonNull Activity activity, int requestCode) {
        if (DbManager.getInstance().getManager().hasAtLeastOneProject()) {
            ProjectPickerActivity.open(activity, requestCode, new CreateTicketProjectPickerController());
        }
        else {
            NewProjectActivity.open(activity, requestCode, new CreateTicketNewProjectController());
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
    public void pickProject(@NonNull Activity activity, @NonNull ProjectId projectId) {
        NewTicketActivity.open(activity, REQUEST_CODE_NEXT_STEP, new CreateTicketForExistingProjectNewTicketController(projectId));
    }

    @Override
    public void newProject(@NonNull Activity activity) {
        NewProjectActivity.open(activity, REQUEST_CODE_NEXT_STEP, new CreateTicketNewProjectController());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        // Nothing to add
    }

    public static final Parcelable.Creator<CreateTicketProjectPickerController> CREATOR = new Parcelable.Creator<CreateTicketProjectPickerController>() {

        @Override
        public CreateTicketProjectPickerController createFromParcel(Parcel source) {
            return new CreateTicketProjectPickerController();
        }

        @Override
        public CreateTicketProjectPickerController[] newArray(int size) {
            return new CreateTicketProjectPickerController[size];
        }
    };
}
