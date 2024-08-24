package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.database.DbIdentifiableQueryBuilder;
import sword.database.DbSettableQueryBuilder;

import static sword.tickets.android.PreconditionUtils.ensureValidArguments;

public final class ReleaseId implements IdInterface {
    final int key;

    public ReleaseId(int key) {
        ensureValidArguments(key != 0);
        this.key = key;
    }

    @Override
    public int hashCode() {
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (!(obj instanceof ReleaseId)) {
            return false;
        }

        final ReleaseId that = (ReleaseId) obj;
        return key == that.key;
    }

    @NonNull
    @Override
    public String toString() {
        return "ReleaseId(" + key + ")";
    }

    @Override
    public void put(int columnIndex, @NonNull DbSettableQueryBuilder builder) {
        builder.put(columnIndex, key);
    }

    @Override
    public void where(int columnIndex, @NonNull DbIdentifiableQueryBuilder builder) {
        builder.where(columnIndex, key);
    }
}
