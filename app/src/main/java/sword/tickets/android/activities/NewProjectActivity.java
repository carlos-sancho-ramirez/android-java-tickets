package sword.tickets.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import sword.tickets.android.layout.NewProjectLayoutForActivity;

import static sword.tickets.android.PreconditionUtils.ensureValidState;
import static sword.tickets.android.activities.ActivityUtils.applyMainInsets;

public final class NewProjectActivity extends Activity {

    private interface ArgKeys {
        String CONTROLLER = "controller";
    }

    public static void open(@NonNull Activity activity, int requestCode, @NonNull Controller controller) {
        final Intent intent = new Intent(activity, NewProjectActivity.class);
        intent.putExtra(ArgKeys.CONTROLLER, controller);
        activity.startActivityForResult(intent, requestCode);
    }

    @NonNull
    private Controller getController() {
        final Controller controller = getIntent().getParcelableExtra(ArgKeys.CONTROLLER, Controller.class);
        ensureValidState(controller != null);
        return controller;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final NewProjectLayoutForActivity layout = NewProjectLayoutForActivity.attach(this);
        applyMainInsets(layout.scrollView());

        layout.nextButton().setOnClickListener(v ->
                getController().complete(this, layout.projectNameField().getText().toString()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getController().onActivityResult(this, requestCode, resultCode, data);
    }

    public interface Controller extends Parcelable {
        void onActivityResult(@NonNull Activity activity, int requestCode, int resultCode, Intent data);
        void complete(@NonNull Activity activity, String name);
    }
}
