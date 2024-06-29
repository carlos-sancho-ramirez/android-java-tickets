package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.database.DbValue;

public final class ProjectIdManager implements IntSetter<ProjectId> {
    @Override
    public ProjectId getKeyFromInt(int key) {
        return (key != 0)? new ProjectId(key) : null;
    }

    @Override
    public ProjectId getKeyFromDbValue(@NonNull DbValue value) {
        return getKeyFromInt(value.toInt());
    }
}
