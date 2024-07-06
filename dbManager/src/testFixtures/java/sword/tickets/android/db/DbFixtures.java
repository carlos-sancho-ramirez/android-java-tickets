package sword.tickets.android.db;

import sword.database.Database;
import sword.database.DbInsertQuery;
import sword.tickets.android.db.TicketsDbSchema.Tables;
import sword.tickets.android.db.TicketsDbSchema.TicketState;
import sword.tickets.android.db.TicketsDbSchema.TicketType;

import androidx.annotation.NonNull;

public final class DbFixtures {

    public static int newProject(@NonNull Database db, String name) {
        final TicketsDbSchema.ProjectsTable table = Tables.projects;
        return db.insert(new DbInsertQuery.Builder(table)
                .put(table.getNameColumnIndex(), name)
                .build());
    }

    public static int newTicket(@NonNull Database db, String name, String description, int projectId, @NonNull TicketType type, int priority) {
        final TicketsDbSchema.TicketsTable table = Tables.tickets;
        return db.insert(new DbInsertQuery.Builder(table)
                .put(table.getNameColumnIndex(), name)
                .put(table.getDescriptionColumnIndex(), description)
                .put(table.getProjectColumnIndex(), projectId)
                .put(table.getTypeColumnIndex(), type.value)
                .put(table.getStateColumnIndex(), TicketState.NOT_STARTED.value)
                .put(table.getPriorityColumnIndex(), priority)
                .build());
    }

    private DbFixtures() {
    }
}
