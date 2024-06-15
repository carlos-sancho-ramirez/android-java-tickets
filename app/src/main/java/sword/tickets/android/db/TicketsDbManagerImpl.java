package sword.tickets.android.db;

import androidx.annotation.NonNull;

public final class TicketsDbManagerImpl extends TicketsDatabaseManager<TicketId> {
    public TicketsDbManagerImpl(@NonNull sword.database.Database db) {
        super(db, new TicketIdManager());
    }
}