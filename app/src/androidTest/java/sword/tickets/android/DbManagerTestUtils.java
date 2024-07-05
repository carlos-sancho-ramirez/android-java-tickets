package sword.tickets.android;

import sword.collections.Function;
import sword.collections.Procedure;
import sword.database.Database;
import sword.database.MemoryDatabase;

import androidx.annotation.NonNull;

public final class DbManagerTestUtils {

    public static void withMemoryDatabase(@NonNull Procedure<Database> procedure) {
        final Function<DbManager, Database> originalDatabaseCreator = DbManager.databaseCreator;
        final MemoryDatabase database = new MemoryDatabase();
        DbManager.databaseCreator = manager -> database;

        final DbManager dbManager = DbManager.getInstance();
        dbManager._database = null;
        dbManager._ticketsManager = null;
        try {
            procedure.apply(database);
        }
        finally {
            DbManager.databaseCreator = originalDatabaseCreator;
            dbManager._database = null;
            dbManager._ticketsManager = null;
        }
    }

    private DbManagerTestUtils() {
    }
}
