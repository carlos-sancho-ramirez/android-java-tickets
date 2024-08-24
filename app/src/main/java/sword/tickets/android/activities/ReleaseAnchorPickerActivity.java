package sword.tickets.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ListView;

import androidx.annotation.NonNull;

import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.tickets.android.DbManager;
import sword.tickets.android.db.ProjectId;
import sword.tickets.android.db.ReleaseId;
import sword.tickets.android.db.TicketsDbSchema.ReleaseType;
import sword.tickets.android.layout.ProjectPickerLayoutForActivity;
import sword.tickets.android.list.adapters.ProjectPickerAdapter;
import sword.tickets.android.models.Release;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidState;

public final class ReleaseAnchorPickerActivity extends Activity {

    private interface ArgKeys {
        String CONTROLLER = "controller";
    }

    public static void open(@NonNull Activity activity, int requestCode, @NonNull Controller controller) {
        ensureNonNull(controller);
        final Intent intent = new Intent(activity, ReleaseAnchorPickerActivity.class);
        intent.putExtra(ArgKeys.CONTROLLER, controller);
        activity.startActivityForResult(intent, requestCode);
    }

    private Controller _controller;
    private ImmutableList<Anchor> _possibleAnchors;

    @NonNull
    private Controller getController() {
        final Controller controller = getIntent().getParcelableExtra(ArgKeys.CONTROLLER, Controller.class);
        ensureValidState(controller != null);
        return controller;
    }

    private static final class Anchor {
        @NonNull
        final ReleaseId id;

        @NonNull
        final Release release;

        Anchor(@NonNull ReleaseId id, @NonNull Release release) {
            ensureNonNull(id, release);
            this.id = id;
            this.release = release;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ProjectPickerLayoutForActivity layout = ProjectPickerLayoutForActivity.attach(this);

        _controller = getController();
        final ImmutableMap<ReleaseId, Release> releases = DbManager.getInstance().getManager().getAllReleasesForProject(_controller.getProjectId());
        final ReleaseType releaseType = _controller.getReleaseType();
        final ImmutableMap<ReleaseId, Release> possibleAnchors;
        if (releaseType == ReleaseType.MINOR) {
            possibleAnchors = releases.filter(r -> r.bugFix == 0 && releases.allMatch(rr -> r.major != rr.major || r.minor >= rr.minor));
        }
        else if (releaseType == ReleaseType.BUG_FIX) {
            possibleAnchors = releases.filter(r -> releases.allMatch(rr -> r.major != rr.major || r.minor != rr.minor || r.bugFix >= rr.bugFix));
        }
        else {
            throw new AssertionError();
        }

        _possibleAnchors = possibleAnchors.entries().map(entry -> new Anchor(entry.key(), entry.value())).sort((a, b) -> {
            final Release ra = a.release;
            final Release rb = b.release;
            return ra.major > rb.major || ra.major == rb.major && (ra.minor > rb.minor || ra.minor == rb.minor && ra.bugFix > rb.bugFix);
        });

        final ListView listView = layout.listView();
        listView.setAdapter(new ProjectPickerAdapter(_possibleAnchors.map(anchor -> {
            final Release r = anchor.release;
            return Integer.toString(r.major) + '.' + r.minor + '.' + r.bugFix;
        })));

        listView.setOnItemClickListener((parent, view, position, id) ->
                _controller.pickAnchor(this, _possibleAnchors.valueAt(position).id));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        _controller.onActivityResult(this, requestCode, resultCode, data);
    }

    public interface Controller extends Parcelable {
        @NonNull
        ProjectId getProjectId();

        @NonNull
        ReleaseType getReleaseType();
        void onActivityResult(@NonNull Activity activity, int requestCode, int resultCode, Intent data);
        void pickAnchor(@NonNull Activity activity, @NonNull ReleaseId releaseId);
    }
}
