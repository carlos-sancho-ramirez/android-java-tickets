package sword.tickets.android.db;

import sword.collections.ImmutableList;
import sword.database.DbIndex;
import sword.database.DbIntColumn;
import sword.database.DbSchema;
import sword.database.DbTable;
import sword.database.DbTextColumn;

import androidx.annotation.NonNull;

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

    public enum ReleaseType {
        MAJOR(1),
        MINOR(2),
        BUG_FIX(3);

        public final int value;

        ReleaseType(int value) {
            this.value = value;
        }
    }

    public static final class ReleasesTable extends DbTable {
        private ReleasesTable() {
            super("Releases", new DbIntColumn("majorVersion"), new DbIntColumn("minorVersion"), new DbIntColumn("bugFixVersion"));
        }

        public int getMajorVersionColumnIndex() {
            return 1;
        }

        public int getMinorVersionColumnIndex() {
            return 2;
        }

        public int getBugFixVersionColumnIndex() {
            return 3;
        }
    }

    public enum TicketType {
        NEW_CAPABILITY(1),
        ISSUE(2),
        MODIFICATION(3);

        public final int value;

        TicketType(int value) {
            this.value = value;
        }
    }

    public enum TicketState implements IntEnumValue {
        NOT_STARTED(1),
        IN_PROGRESS(2),
        ABANDONED(3),
        COMPLETED(4);

        public final int value;

        TicketState(int value) {
            this.value = value;
        }

        @Override
        public int value() {
            return value;
        }
    }

    public static final class TicketsTable extends DbTable {
        private TicketsTable() {
            super("Tickets", new DbIntColumn("project"), new DbTextColumn("name"), new DbTextColumn("description"), new DbIntColumn("type"), new DbIntColumn("state"), new DbIntColumn("priority"));
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

        public int getTypeColumnIndex() {
            return 4;
        }

        public int getStateColumnIndex() {
            return 5;
        }

        public int getPriorityColumnIndex() {
            return 6;
        }
    }

    public interface Tables {
        ProjectsTable projects = new ProjectsTable();
        ReleasesTable releases = new ReleasesTable();
        TicketsTable tickets = new TicketsTable();
    }

    private final ImmutableList<DbTable> _tables = new ImmutableList.Builder<DbTable>()
            .append(Tables.projects)
            .append(Tables.releases)
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
