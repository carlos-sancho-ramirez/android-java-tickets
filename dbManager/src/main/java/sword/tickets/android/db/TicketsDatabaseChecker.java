package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.collections.ImmutableIntKeyMap;
import sword.collections.ImmutableMap;
import sword.collections.List;
import sword.collections.MutableHashMap;
import sword.collections.MutableIntKeyMap;
import sword.collections.MutableMap;
import sword.database.Database;
import sword.database.DbQuery;
import sword.database.DbResult;
import sword.database.DbValue;
import sword.tickets.android.db.TicketsDbSchema.Tables;
import sword.tickets.android.db.TicketsDbSchema.TicketsTable;

import static sword.tickets.android.db.PreconditionUtils.ensureNonNull;

public class TicketsDatabaseChecker<TicketId> implements TicketsChecker<TicketId> {

    @NonNull
    private final Database _db;

    @NonNull
    private final IntSetter<TicketId> _ticketIdManager;

    public TicketsDatabaseChecker(@NonNull Database db, @NonNull IntSetter<TicketId> ticketIdManager) {
        ensureNonNull(db, ticketIdManager);
        _db = db;
        _ticketIdManager = ticketIdManager;
    }

    @NonNull
    @Override
    public ImmutableMap<TicketId, String> getAllTickets() {
        final TicketsTable table = Tables.tickets;
        final DbQuery query = new DbQuery.Builder(table)
                .select(table.getIdColumnIndex(), table.getNameColumnIndex());
        final MutableMap<TicketId, String> map = MutableHashMap.empty();
        try (DbResult result = _db.select(query)) {
            while (result.hasNext()) {
                final List<DbValue> row = result.next();
                final TicketId id = _ticketIdManager.getKeyFromDbValue(row.get(0));
                final String name = row.get(1).toString();
                map.put(id, name);
            }
        }

        return map.toImmutable();
    }
}
