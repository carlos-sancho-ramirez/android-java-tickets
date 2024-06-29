package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.collections.ImmutableMap;
import sword.collections.List;
import sword.collections.MutableHashMap;
import sword.collections.MutableMap;
import sword.database.Database;
import sword.database.DbQuery;
import sword.database.DbResult;
import sword.database.DbValue;
import sword.tickets.android.db.TicketsDbSchema.ProjectsTable;
import sword.tickets.android.db.TicketsDbSchema.Tables;
import sword.tickets.android.db.TicketsDbSchema.TicketsTable;
import sword.tickets.android.models.Ticket;

import static sword.tickets.android.db.PreconditionUtils.ensureNonNull;

public class TicketsDatabaseChecker<ProjectId, TicketId extends IdWhereInterface> implements TicketsChecker<ProjectId, TicketId> {

    @NonNull
    final Database _db;

    @NonNull
    final IntSetter<ProjectId> _projectIdManager;

    @NonNull
    final IntSetter<TicketId> _ticketIdManager;

    public TicketsDatabaseChecker(@NonNull Database db, @NonNull IntSetter<ProjectId> projectIdManager, @NonNull IntSetter<TicketId> ticketIdManager) {
        ensureNonNull(db, projectIdManager, ticketIdManager);
        _db = db;
        _projectIdManager = projectIdManager;
        _ticketIdManager = ticketIdManager;
    }

    @NonNull
    @Override
    public final ImmutableMap<ProjectId, String> getAllProjects() {
        final ProjectsTable table = Tables.projects;
        final DbQuery query = new DbQuery.Builder(table)
                .select(table.getIdColumnIndex(), table.getNameColumnIndex());
        final MutableMap<ProjectId, String> map = MutableHashMap.empty();
        try (DbResult result = _db.select(query)) {
            while (result.hasNext()) {
                final List<DbValue> row = result.next();
                final ProjectId id = _projectIdManager.getKeyFromDbValue(row.get(0));
                final String name = row.get(1).toText();
                map.put(id, name);
            }
        }

        return map.toImmutable();
    }

    @Override
    public final boolean hasAtLeastOneProject() {
        final ProjectsTable table = Tables.projects;
        final DbQuery query = new DbQuery.Builder(table)
                .select(table.getIdColumnIndex());
        try (DbResult result = _db.select(query)) {
            return result.hasNext();
        }
    }

    @NonNull
    @Override
    public final ImmutableMap<TicketId, String> getAllTickets() {
        final TicketsTable table = Tables.tickets;
        final DbQuery query = new DbQuery.Builder(table)
                .select(table.getIdColumnIndex(), table.getNameColumnIndex());
        final MutableMap<TicketId, String> map = MutableHashMap.empty();
        try (DbResult result = _db.select(query)) {
            while (result.hasNext()) {
                final List<DbValue> row = result.next();
                final TicketId id = _ticketIdManager.getKeyFromDbValue(row.get(0));
                final String name = row.get(1).toText();
                map.put(id, name);
            }
        }

        return map.toImmutable();
    }

    @Override
    public final Ticket<ProjectId> getTicket(@NonNull TicketId ticketId) {
        final TicketsTable table = Tables.tickets;
        final DbQuery query = new DbQueryBuilder(table)
                .where(table.getIdColumnIndex(), ticketId)
                .select(table.getNameColumnIndex(), table.getDescriptionColumnIndex(), table.getProjectColumnIndex());

        try (DbResult result = _db.select(query)) {
            if (result.hasNext()) {
                final List<DbValue> row = result.next();
                final String name = row.get(0).toText();
                final String description = row.get(1).toText();
                final ProjectId projectId = _projectIdManager.getKeyFromDbValue(row.get(2));
                return new Ticket<>(name, description, projectId);
            }
            else {
                return null;
            }
        }
    }
}
