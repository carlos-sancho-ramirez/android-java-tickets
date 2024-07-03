package sword.tickets.android;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import sword.tickets.android.db.ProjectId;
import sword.tickets.android.db.ProjectIdManager;

import static sword.tickets.android.PreconditionUtils.ensureNonNull;

public final class UserPreferences {

    private static final String PREFERENCES_NAME = "TicketsPreferences";

    private interface SavedKeys {
        String SELECTED_PROJECT = "sp";
    }

    @NonNull
    private final Context _context;

    public UserPreferences(@NonNull Context context) {
        ensureNonNull(context);
        _context = context;
    }

    public ProjectId getSelectedProject() {
        return new ProjectIdManager().getKeyFromInt(
                _context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(SavedKeys.SELECTED_PROJECT, 0));
    }

    public void setSelectedProject(ProjectId projectId) {
        final SharedPreferences.Editor editor = _context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        if (projectId == null) {
            editor.remove(PREFERENCES_NAME);
        }
        else {
            projectId.put(editor, PREFERENCES_NAME);
        }
        editor.apply();
    }
}
