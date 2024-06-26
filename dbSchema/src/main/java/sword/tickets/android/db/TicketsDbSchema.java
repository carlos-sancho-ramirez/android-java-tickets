package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.collections.ImmutableList;
import sword.database.DbIndex;
import sword.database.DbIntColumn;
import sword.database.DbSchema;
import sword.database.DbTable;
import sword.database.DbTextColumn;

public final class TicketsDbSchema implements DbSchema {

    private static TicketsDbSchema _instance;

    public static TicketsDbSchema getInstance() {
        if (_instance == null) {
            _instance = new TicketsDbSchema();
        }

        return _instance;
    }

    public static final class ProjectsTable extends DbTable {
        private ProjectsTable() {
            super("Projects", new DbTextColumn("name"));
        }

        public int getNameColumnIndex() {
            return 1;
        }
    }

    public static final class TicketsTable extends DbTable {
        private TicketsTable() {
            super("Tickets", new DbIntColumn("project"), new DbTextColumn("name"), new DbTextColumn("description"));
        }

        public int getProjectColumnIndex() {
            return 1;
        }

        public int getNameColumnIndex() {
            return 2;
        }

        public int getDescriptionColumnIndex() {
            return 3;
        }
    }

    public interface Tables {
        ProjectsTable projects = new ProjectsTable();
        TicketsTable tickets = new TicketsTable();
    }

    private final ImmutableList<DbTable> _tables = new ImmutableList.Builder<DbTable>()
            .append(Tables.projects)
            .append(Tables.tickets)
            .build();

    private TicketsDbSchema() {
    }

    public int currentSchemaVersionCode() {
        return 1;
    }

    @NonNull
    @Override
    public ImmutableList<DbTable> tables() {
        return _tables;
    }

    @NonNull
    @Override
    public ImmutableList<DbIndex> indexes() {
        return ImmutableList.empty();
    }
}
