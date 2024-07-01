package sword.tickets.android.db;

import sword.database.DbValue;

import androidx.annotation.NonNull;

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
