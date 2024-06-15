package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.database.Database;

public class TicketsDatabaseManager<TicketId> extends TicketsDatabaseChecker<TicketId> implements TicketsManager<TicketId> {
    public TicketsDatabaseManager(@NonNull Database db, @NonNull IntSetter<TicketId> ticketIdManager) {
        super(db, ticketIdManager);
    }
}
