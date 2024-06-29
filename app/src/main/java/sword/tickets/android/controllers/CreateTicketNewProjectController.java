package sword.tickets.android.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import sword.tickets.android.activities.NewProjectActivity;
import sword.tickets.android.activities.NewTicketActivity;

public final class CreateTicketNewProjectController implements NewProjectActivity.Controller {

    private static final int REQUEST_CODE_NEXT_STEP = 1;

    @Override
    public void onActivityResult(@NonNull Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_NEXT_STEP && resultCode == Activity.RESULT_OK) {
            activity.setResult(Activity.RESULT_OK, data);
            activity.finish();
        }
    }

    @Override
    public void complete(@NonNull Activity activity, String name) {
        NewTicketActivity.open(activity, REQUEST_CODE_NEXT_STEP, new CreateTicketForNewProjectNewTicketController(name));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        // Nothing to be done
    }

    public static final Parcelable.Creator<CreateTicketNewProjectController> CREATOR = new Parcelable.Creator<CreateTicketNewProjectController>() {

        @Override
        public CreateTicketNewProjectController createFromParcel(Parcel source) {
            return new CreateTicketNewProjectController();
        }

        @Override
        public CreateTicketNewProjectController[] newArray(int size) {
            return new CreateTicketNewProjectController[size];
        }
    };
}
