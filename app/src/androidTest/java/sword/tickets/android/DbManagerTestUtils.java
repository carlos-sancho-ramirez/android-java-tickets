package sword.tickets.android;

import sword.collections.Procedure;
import sword.database.Database;
import sword.database.MemoryDatabase;

import androidx.annotation.NonNull;

public final class DbManagerTestUtils {

    public static void withMemoryDatabase(@NonNull Procedure<Database> procedure) {
        final DbManager dbManager = DbManager.getInstance();
        final Database originalDatabase = dbManager._database;
        final MemoryDatabase database = new MemoryDatabase();
        dbManager._database = database;
        dbManager._ticketsManager = null;
        try {
            procedure.apply(database);
        }
        finally {
            dbManager._database = originalDatabase;
        }
    }

    private DbManagerTestUtils() {
    }
}
