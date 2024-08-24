package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.collections.List;
import sword.collections.MutableIntList;
import sword.collections.MutableList;
import sword.database.Database;
import sword.database.DbInsertQuery;
import sword.database.DbQuery;
import sword.database.DbQuery.Ordered;
import sword.database.DbResult;
import sword.database.DbValue;
import sword.tickets.android.db.TicketsDbSchema.ProjectsTable;
import sword.tickets.android.db.TicketsDbSchema.ReleasesTable;
import sword.tickets.android.db.TicketsDbSchema.Tables;
import sword.tickets.android.db.TicketsDbSchema.TicketType;
import sword.tickets.android.db.TicketsDbSchema.TicketsTable;
import sword.tickets.android.models.Ticket;

import static sword.tickets.android.db.PreconditionUtils.ensureValidState;

public class TicketsDatabaseManager<ProjectId extends IdInterface, ReleaseId extends IdInterface, TicketId extends IdInterface> extends TicketsDatabaseChecker<ProjectId, ReleaseId, TicketId> implements TicketsManager<ProjectId, ReleaseId, TicketId> {
    public TicketsDatabaseManager(@NonNull Database db, @NonNull IntSetter<ProjectId> projectIdManager, @NonNull IntSetter<ReleaseId> releaseIdManager, @NonNull IntSetter<TicketId> ticketIdManager) {
        super(db, projectIdManager, releaseIdManager, ticketIdManager);
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
    public final ReleaseId newRelease(@NonNull ProjectId project, int major, int minor, int bugFix) {
        final ReleasesTable table = Tables.releases;
        final int newId = _db.insert(new DbInsertQueryBuilder(table)
                .put(table.getProjectColumnIndex(), project)
                .put(table.getMajorVersionColumnIndex(), major)
                .put(table.getMinorVersionColumnIndex(), minor)
                .put(table.getBugFixVersionColumnIndex(), bugFix)
                .build());
        ensureValidState(newId != 0);
        return _releaseIdManager.getKeyFromInt(newId);
    }

    @NonNull
    @Override
    public final TicketId newTicket(String name, String description, @NonNull ProjectId projectId, @NonNull TicketType type) {
        final TicketsTable table = Tables.tickets;
        int maxPriority = 0;
        try (DbResult dbResult = _db.select(new DbQueryBuilder(table).select(table.getPriorityColumnIndex()))) {
            while (dbResult.hasNext()) {
                maxPriority = Math.max(maxPriority, dbResult.next().get(0).toInt());
            }
        }

        final int newId = _db.insert(new DbInsertQueryBuilder(table)
                .put(table.getNameColumnIndex(), name)
                .put(table.getDescriptionColumnIndex(), description)
                .put(table.getProjectColumnIndex(), projectId)
                .put(table.getReleaseColumnIndex(), 0)
                .put(table.getTypeColumnIndex(), type.value)
                .put(table.getStateColumnIndex(), TicketsDbSchema.TicketState.NOT_STARTED.value)
                .put(table.getPriorityColumnIndex(), maxPriority + 1)
                .build());

        ensureValidState(newId != 0);
        return _ticketIdManager.getKeyFromInt(newId);
    }

    @Override
    public final boolean updateTicket(@NonNull TicketId ticketId, @NonNull Ticket<ProjectId, ReleaseId> ticket) {
        final TicketsTable table = Tables.tickets;
        return _db.update(new DbUpdateQueryBuilder(table)
                .where(table.getIdColumnIndex(), ticketId)
                .put(table.getNameColumnIndex(), ticket.name)
                .put(table.getDescriptionColumnIndex(), ticket.description)
                .put(table.getProjectColumnIndex(), ticket.projectId)
                .put(table.getReleaseColumnIndex(), ticket.releaseId)
                .put(table.getStateColumnIndex(), ticket.state.value)
                .build());
    }

    @Override
    public final boolean updateTicketRelease(@NonNull TicketId ticketId, ReleaseId releaseId) {
        final TicketsTable table = Tables.tickets;
        return _db.update(new DbUpdateQueryBuilder(table)
                .where(table.getIdColumnIndex(), ticketId)
                .put(table.getReleaseColumnIndex(), releaseId)
                .build());
    }

    public final boolean deleteTicket(@NonNull TicketId ticketId) {
        final TicketsTable table = Tables.tickets;
        return _db.delete(new DbDeleteQueryBuilder(table)
                .where(table.getIdColumnIndex(), ticketId)
                .build());
    }

    public final boolean moveTicket(@NonNull ProjectId projectId, int movingPosition, int gapPosition) {
        ensureValidState(movingPosition >= 0);
        ensureValidState(gapPosition >= 0);

        if (movingPosition == gapPosition) {
            return true;
        }

        final TicketsTable table = Tables.tickets;

        final DbQuery query = new DbQueryBuilder(table)
                .where(table.getProjectColumnIndex(), projectId)
                .orderBy(new Ordered(table.getPriorityColumnIndex(), true))
                .select(table.getIdColumnIndex(), table.getPriorityColumnIndex());

        final MutableList<TicketId> tickets = MutableList.empty();
        final MutableIntList priorities = MutableIntList.empty();
        try (DbResult dbResult = _db.select(query)) {
            while (dbResult.hasNext()) {
                final List<DbValue> row = dbResult.next();
                tickets.append(_ticketIdManager.getKeyFromDbValue(row.get(0)));
                priorities.append(row.get(1).toInt());
            }
        }

        final int ticketsCount = tickets.size();
        if (movingPosition >= ticketsCount || gapPosition >= ticketsCount) {
            return false;
        }

        if (movingPosition < gapPosition) {
            final int topPriority = priorities.valueAt(movingPosition);
            for (int i = movingPosition; i < gapPosition; i++) {
                if (!_db.update(new DbUpdateQueryBuilder(table)
                        .where(table.getIdColumnIndex(), tickets.valueAt(i + 1))
                        .put(table.getPriorityColumnIndex(), topPriority - i + movingPosition)
                        .build())) {
                    throw new AssertionError("Unable to update ticket");
                }
            }

            if (!_db.update(new DbUpdateQueryBuilder(table)
                    .where(table.getIdColumnIndex(), tickets.valueAt(movingPosition))
                    .put(table.getPriorityColumnIndex(), topPriority - gapPosition + movingPosition)
                    .build())) {
                throw new AssertionError("Unable to update ticket");
            }
        }
        else {
            final int topPriority = priorities.valueAt(gapPosition);
            if (!_db.update(new DbUpdateQueryBuilder(table)
                    .where(table.getIdColumnIndex(), tickets.valueAt(movingPosition))
                    .put(table.getPriorityColumnIndex(), topPriority)
                    .build())) {
                throw new AssertionError("Unable to update ticket");
            }

            for (int i = gapPosition + 1; i <= movingPosition; i++) {
                if (!_db.update(new DbUpdateQueryBuilder(table)
                        .where(table.getIdColumnIndex(), tickets.valueAt(i - 1))
                        .put(table.getPriorityColumnIndex(), topPriority - i + gapPosition)
                        .build())) {
                    throw new AssertionError("Unable to update ticket");
                }
            }
        }

        return true;
    }
}
