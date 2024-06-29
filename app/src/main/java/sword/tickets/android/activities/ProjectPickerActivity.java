package sword.tickets.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;

import sword.collections.ImmutableMap;
import sword.tickets.android.DbManager;
import sword.tickets.android.R;
import sword.tickets.android.db.ProjectId;
import sword.tickets.android.layout.ProjectPickerLayoutForActivity;
import sword.tickets.android.list.adapters.ProjectPickerAdapter;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;
import static sword.tickets.android.PreconditionUtils.ensureValidState;

public final class ProjectPickerActivity extends Activity {

    private interface ArgKeys {
        String CONTROLLER = "controller";
    }

    public static void open(@NonNull Activity activity, int requestCode, @NonNull Controller controller) {
        ensureNonNull(controller);
        final Intent intent = new Intent(activity, ProjectPickerActivity.class);
        intent.putExtra(ArgKeys.CONTROLLER, controller);
        activity.startActivityForResult(intent, requestCode);
    }

    private ImmutableMap<ProjectId, String> _projects;

    @NonNull
    private Controller getController() {
        final Controller controller = getIntent().getParcelableExtra(ArgKeys.CONTROLLER, Controller.class);
        ensureValidState(controller != null);
        return controller;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ProjectPickerLayoutForActivity layout = ProjectPickerLayoutForActivity.attach(this);

        _projects = DbManager.getInstance().getManager().getAllProjects();
        final ListView listView = layout.listView();
        listView.setAdapter(new ProjectPickerAdapter(_projects.toList()));
        listView.setOnItemClickListener((parent, view, position, id) ->
                getController().pickProject(this, _projects.keyAt(position)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getController().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.project_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.optionNew) {
            getController().newProject(this);
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    public interface Controller extends Parcelable {
        void onActivityResult(@NonNull Activity activity, int requestCode, int resultCode, Intent data);
        void pickProject(@NonNull Activity activity, @NonNull ProjectId projectId);
        void newProject(@NonNull Activity activity);
    }
}
