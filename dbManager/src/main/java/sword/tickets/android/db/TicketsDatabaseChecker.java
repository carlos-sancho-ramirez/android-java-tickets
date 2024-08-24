package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.List;
import sword.collections.MutableHashMap;
import sword.collections.MutableHashSet;
import sword.collections.MutableList;
import sword.collections.MutableMap;
import sword.collections.MutableSet;
import sword.database.Database;
import sword.database.DbQuery;
import sword.database.DbQuery.Ordered;
import sword.database.DbResult;
import sword.database.DbValue;
import sword.tickets.android.db.TicketsDbSchema.ProjectsTable;
import sword.tickets.android.db.TicketsDbSchema.ReleasesTable;
import sword.tickets.android.db.TicketsDbSchema.Tables;
import sword.tickets.android.db.TicketsDbSchema.TicketsTable;
import sword.tickets.android.models.Release;
import sword.tickets.android.models.Ticket;
import sword.tickets.android.models.TicketReference;

import static sword.tickets.android.db.PreconditionUtils.ensureNonNull;

public class TicketsDatabaseChecker<ProjectId extends IdWhereInterface, ReleaseId extends IdWhereInterface, TicketId extends IdWhereInterface> implements TicketsChecker<ProjectId, ReleaseId, TicketId> {

    @NonNull
    final Database _db;

    @NonNull
    final IntSetter<ProjectId> _projectIdManager;

    @NonNull
    final IntSetter<ReleaseId> _releaseIdManager;

    @NonNull
    final IntSetter<TicketId> _ticketIdManager;

    public TicketsDatabaseChecker(@NonNull Database db, @NonNull IntSetter<ProjectId> projectIdManager, @NonNull IntSetter<ReleaseId> releaseIdManager, @NonNull IntSetter<TicketId> ticketIdManager) {
        ensureNonNull(db, projectIdManager, releaseIdManager, ticketIdManager);
        _db = db;
        _projectIdManager = projectIdManager;
        _releaseIdManager = releaseIdManager;
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

    @NonNull
    @Override
    public final ImmutableMap<ReleaseId, Release> getAllReleasesForProject(@NonNull ProjectId projectId) {
        final ReleasesTable table = Tables.releases;
        final DbQuery query = new DbQueryBuilder(table)
                .where(table.getProjectColumnIndex(), projectId)
                .select(
                        table.getIdColumnIndex(),
                        table.getMajorVersionColumnIndex(),
                        table.getMinorVersionColumnIndex(),
                        table.getBugFixVersionColumnIndex());
        final MutableMap<ReleaseId, Release> map = MutableHashMap.empty();
        try (DbResult result = _db.select(query)) {
            while (result.hasNext()) {
                final List<DbValue> row = result.next();
                final ReleaseId id = _releaseIdManager.getKeyFromDbValue(row.get(0));
                final int major = row.get(1).toInt();
                final int minor = row.get(2).toInt();
                final int bugFix = row.get(3).toInt();
                map.put(id, new Release(major, minor, bugFix));
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

    @Override
    public final boolean hasAtLeastOneRelease() {
        final ReleasesTable table = Tables.releases;
        final DbQuery query = new DbQuery.Builder(table)
                .select(table.getIdColumnIndex());
        try (DbResult result = _db.select(query)) {
            return result.hasNext();
        }
    }

    @NonNull
    @Override
    public final ImmutableList<TicketReference<TicketId>> getAllTicketReferences() {
        final TicketsTable table = Tables.tickets;
        final DbQuery query = new DbQuery.Builder(table)
                .orderBy(new Ordered(table.getPriorityColumnIndex(), true))
                .select(table.getIdColumnIndex(), table.getNameColumnIndex());
        final MutableList<TicketReference<TicketId>> list = MutableList.empty();
        try (DbResult result = _db.select(query)) {
            while (result.hasNext()) {
                final List<DbValue> row = result.next();
                final TicketId id = _ticketIdManager.getKeyFromDbValue(row.get(0));
                final String name = row.get(1).toText();
                list.append(new TicketReference<>(id, name));
            }
        }

        return list.toImmutable();
    }

    @NonNull
    @Override
    public final ImmutableList<TicketReference<TicketId>> getAllTicketReferencesForProject(@NonNull ProjectId projectId) {
        final TicketsTable table = Tables.tickets;
        final DbQuery query = new DbQueryBuilder(table)
                .where(table.getProjectColumnIndex(), projectId)
                .orderBy(new Ordered(table.getPriorityColumnIndex(), true))
                .select(table.getIdColumnIndex(), table.getNameColumnIndex());
        final MutableList<TicketReference<TicketId>> list = MutableList.empty();
        try (DbResult result = _db.select(query)) {
            while (result.hasNext()) {
                final List<DbValue> row = result.next();
                final TicketId id = _ticketIdManager.getKeyFromDbValue(row.get(0));
                final String name = row.get(1).toText();
                list.append(new TicketReference<>(id, name));
            }
        }

        return list.toImmutable();
    }

    @NonNull
    @Override
    public final ImmutableSet<TicketReference<TicketId>> getAllTicketReferencesWithoutReleaseForProject(@NonNull ProjectId projectId) {
        final TicketsTable table = Tables.tickets;
        final DbQuery query = new DbQueryBuilder(table)
                .where(table.getProjectColumnIndex(), projectId)
                .where(table.getReleaseColumnIndex(), 0)
                .select(table.getIdColumnIndex(), table.getNameColumnIndex());
        final MutableSet<TicketReference<TicketId>> set = MutableHashSet.empty();
        try (DbResult result = _db.select(query)) {
            while (result.hasNext()) {
                final List<DbValue> row = result.next();
                final TicketId id = _ticketIdManager.getKeyFromDbValue(row.get(0));
                final String name = row.get(1).toText();
                set.add(new TicketReference<>(id, name));
            }
        }

        return set.toImmutable();
    }

    @Override
    public final boolean hasAtLeastOneTicketWithoutReleaseForProject(@NonNull ProjectId projectId) {
        final TicketsTable table = Tables.tickets;
        final DbQuery query = new DbQueryBuilder(table)
                .where(table.getProjectColumnIndex(), projectId)
                .where(table.getReleaseColumnIndex(), 0)
                .select(table.getIdColumnIndex());

        try (DbResult result = _db.select(query)) {
            return result.hasNext();
        }
    }

    @Override
    public final Release getRelease(@NonNull ReleaseId id) {
        final ReleasesTable table = Tables.releases;
        final DbQuery query = new DbQueryBuilder(table)
                .where(table.getIdColumnIndex(), id)
                .select(
                        table.getMajorVersionColumnIndex(),
                        table.getMinorVersionColumnIndex(),
                        table.getBugFixVersionColumnIndex());

        try (DbResult result = _db.select(query)) {
            if (result.hasNext()) {
                final List<DbValue> row = result.next();
                final int major = row.get(0).toInt();
                final int minor = row.get(1).toInt();
                final int bugFix = row.get(2).toInt();
                return new Release(major, minor, bugFix);
            }
            else {
                return null;
            }
        }
    }

    @Override
    public final Ticket<ProjectId, ReleaseId> getTicket(@NonNull TicketId ticketId) {
        final TicketsTable table = Tables.tickets;
        final DbQuery query = new DbQueryBuilder(table)
                .where(table.getIdColumnIndex(), ticketId)
                .select(
                        table.getNameColumnIndex(),
                        table.getDescriptionColumnIndex(),
                        table.getProjectColumnIndex(),
                        table.getReleaseColumnIndex(),
                        table.getStateColumnIndex());

        try (DbResult result = _db.select(query)) {
            if (result.hasNext()) {
                final List<DbValue> row = result.next();
                final String name = row.get(0).toText();
                final String description = row.get(1).toText();
                final ProjectId projectId = _projectIdManager.getKeyFromDbValue(row.get(2));
                final ReleaseId releaseId = _releaseIdManager.getKeyFromDbValue(row.get(3));
                final int rawState = row.get(4).toInt();
                final TicketsDbSchema.TicketState state = ImmutableList.from(TicketsDbSchema.TicketState.values()).findFirst(st -> rawState == st.value, null);
                return new Ticket<>(name, description, projectId, releaseId, state);
            }
            else {
                return null;
            }
        }
    }
}
