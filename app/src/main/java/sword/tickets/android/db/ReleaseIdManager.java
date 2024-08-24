package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.database.DbValue;

public final class ReleaseIdManager implements IntSetter<ReleaseId> {
    @Override
    public ReleaseId getKeyFromInt(int key) {
        return (key != 0)? new ReleaseId(key) : null;
    }

    @Override
    public ReleaseId getKeyFromDbValue(@NonNull DbValue value) {
        return getKeyFromInt(value.toInt());
    }
}
