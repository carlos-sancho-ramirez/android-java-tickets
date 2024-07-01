package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.database.Database;
import sword.database.DbInsertQuery;
import sword.tickets.android.db.TicketsDbSchema.ProjectsTable;
import sword.tickets.android.db.TicketsDbSchema.Tables;
import sword.tickets.android.db.TicketsDbSchema.TicketType;
import sword.tickets.android.db.TicketsDbSchema.TicketsTable;
import sword.tickets.android.models.Ticket;

import static sword.tickets.android.db.PreconditionUtils.ensureValidState;

public class TicketsDatabaseManager<ProjectId extends IdPutInterface, TicketId extends IdInterface> extends TicketsDatabaseChecker<ProjectId, TicketId> implements TicketsManager<ProjectId, TicketId> {
    public TicketsDatabaseManager(@NonNull Database db, @NonNull IntSetter<ProjectId> projectIdManager, @NonNull IntSetter<TicketId> ticketIdManager) {
        super(db, projectIdManager, ticketIdManager);
    }

    @NonNull
    @Override
    public final ProjectId newProject(String name) {
        final ProjectsTable table = Tables.projects;
        final int newId = _db.insert(new DbInsertQuery.Builder(table)
                .put(table.getNameColumnIndex(), name)
                .build());
        ensureValidState(newId != 0);
        return _projectIdManager.getKeyFromInt(newId);
    }

    @NonNull
    @Override
    public final TicketId newTicket(String name, String description, @NonNull ProjectId projectId, @NonNull TicketType type) {
        final TicketsTable table = Tables.tickets;
        final int newId = _db.insert(new DbInsertQueryBuilder(table)
                .put(table.getNameColumnIndex(), name)
                .put(table.getDescriptionColumnIndex(), description)
                .put(table.getProjectColumnIndex(), projectId)
                .put(table.getTypeColumnIndex(), type.value)
                .build());

        ensureValidState(newId != 0);
        return _ticketIdManager.getKeyFromInt(newId);
    }

    @Override
    public final boolean updateTicket(@NonNull TicketId ticketId, @NonNull Ticket<ProjectId> ticket) {
        final TicketsTable table = Tables.tickets;
        return _db.update(new DbUpdateQueryBuilder(table)
                .where(table.getIdColumnIndex(), ticketId)
                .put(table.getNameColumnIndex(), ticket.name)
                .put(table.getDescriptionColumnIndex(), ticket.description)
                .put(table.getProjectColumnIndex(), ticket.projectId)
                .build());
    }

    public final boolean deleteTicket(@NonNull TicketId ticketId) {
        final TicketsTable table = Tables.tickets;
        return _db.delete(new DbDeleteQueryBuilder(table)
                .where(table.getIdColumnIndex(), ticketId)
                .build());
    }
}
