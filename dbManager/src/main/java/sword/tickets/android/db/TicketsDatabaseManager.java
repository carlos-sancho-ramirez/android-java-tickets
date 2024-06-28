package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.database.Database;
import sword.database.DbInsertQuery;
import sword.tickets.android.db.TicketsDbSchema.Tables;
import sword.tickets.android.db.TicketsDbSchema.TicketsTable;
import sword.tickets.android.models.Ticket;

public class TicketsDatabaseManager<TicketId extends IdInterface> extends TicketsDatabaseChecker<TicketId> implements TicketsManager<TicketId> {
    public TicketsDatabaseManager(@NonNull Database db, @NonNull IntSetter<TicketId> ticketIdManager) {
        super(db, ticketIdManager);
    }

    @Override
    public final void newTicket(String name, String description) {
        final TicketsTable table = Tables.tickets;
        _db.insert(new DbInsertQuery.Builder(table)
                .put(table.getNameColumnIndex(), name)
                .put(table.getDescriptionColumnIndex(), description)
                .put(table.getProjectColumnIndex(), 1)
                .build());
    }

    @Override
    public final boolean updateTicket(@NonNull TicketId ticketId, @NonNull Ticket ticket) {
        final TicketsTable table = Tables.tickets;
        return _db.update(new DbUpdateQueryBuilder(table)
                .where(table.getIdColumnIndex(), ticketId)
                .put(table.getNameColumnIndex(), ticket.name)
                .put(table.getDescriptionColumnIndex(), ticket.description)
                .build());
    }

    public final boolean deleteTicket(@NonNull TicketId ticketId) {
        final TicketsTable table = Tables.tickets;
        return _db.delete(new DbDeleteQueryBuilder(table)
                .where(table.getIdColumnIndex(), ticketId)
                .build());
    }
}
