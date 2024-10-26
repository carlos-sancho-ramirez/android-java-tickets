package sword.tickets.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import sword.tickets.android.R;
import sword.tickets.android.db.TicketsDbSchema.ReleaseType;
import sword.tickets.android.layout.ReleaseTypePickerLayoutForActivity;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidState;
import static sword.tickets.android.activities.ActivityUtils.applyMainInsets;

public final class ReleaseTypePickerActivity extends Activity {

    private interface ArgKeys {
        String CONTROLLER = "controller";
    }

    public static void open(@NonNull Activity activity, int requestCode, @NonNull Controller controller) {
        ensureNonNull(controller);
        final Intent intent = new Intent(activity, ReleaseTypePickerActivity.class);
        intent.putExtra(ArgKeys.CONTROLLER, controller);
        activity.startActivityForResult(intent, requestCode);
    }

    private Controller _controller;

    @NonNull
    private Controller getController() {
        final Controller controller = getIntent().getParcelableExtra(ArgKeys.CONTROLLER, Controller.class);
        ensureValidState(controller != null);
        return controller;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ReleaseTypePickerLayoutForActivity layout = ReleaseTypePickerLayoutForActivity.attach(this);
        applyMainInsets(layout.mainContainer());

        _controller = getController();
        final TextView infoBox = layout.releaseTypeInfoBox();
        final Button nextButton = layout.nextButton();
        layout.majorReleaseButton().setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                infoBox.setText(R.string.releaseTypeMajorInfo);
                nextButton.setEnabled(true);
            }
        });
        layout.minorReleaseButton().setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                infoBox.setText(R.string.releaseTypeMinorInfo);
                nextButton.setEnabled(true);
            }
        });
        layout.bugFixReleaseButton().setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                infoBox.setText(R.string.releaseTypeBugFixInfo);
                nextButton.setEnabled(true);
            }
        });

        layout.nextButton().setOnClickListener(v -> {
            final int id = layout.radioButtonGroup().getCheckedRadioButtonId();
            final ReleaseType releaseType = (id == R.id.majorReleaseButton)? ReleaseType.MAJOR :
                    (id == R.id.minorReleaseButton)? ReleaseType.MINOR : ReleaseType.BUG_FIX;
            _controller.pickReleaseType(this, releaseType);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        _controller.onActivityResult(this, requestCode, resultCode, data);
    }

    public interface Controller extends Parcelable {
        void onActivityResult(@NonNull Activity activity, int requestCode, int resultCode, Intent data);
        void pickReleaseType(@NonNull Activity activity, @NonNull ReleaseType releaseType);
    }
}
