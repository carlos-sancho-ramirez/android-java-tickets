package sword.tickets.android.db;

import sword.database.DbValue;

import androidx.annotation.NonNull;

public final class TicketIdManager implements IntSetter<TicketId> {
    @Override
    public TicketId getKeyFromInt(int key) {
        return (key != 0)? new TicketId(key) : null;
    }

    @Override
    public TicketId getKeyFromDbValue(@NonNull DbValue value) {
        return getKeyFromInt(value.toInt());
    }
}
