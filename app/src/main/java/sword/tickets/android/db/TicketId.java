package sword.tickets.android.db;

import sword.database.DbIdentifiableQueryBuilder;
import sword.database.DbSettableQueryBuilder;

import androidx.annotation.NonNull;

import static sword.tickets.android.PreconditionUtils.ensureValidArguments;

public final class TicketId implements IdInterface {
    final int key;

    public TicketId(int key) {
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
        else if (!(obj instanceof TicketId)) {
            return false;
        }

        final TicketId that = (TicketId) obj;
        return key == that.key;
    }

    @NonNull
    @Override
    public String toString() {
        return "TicketId(" + key + ")";
    }

    @Override
    public void where(int columnIndex, @NonNull DbIdentifiableQueryBuilder builder) {
        builder.where(columnIndex, key);
    }

    @Override
    public void put(int columnIndex, @NonNull DbSettableQueryBuilder builder) {
        builder.put(columnIndex, key);
    }
}
