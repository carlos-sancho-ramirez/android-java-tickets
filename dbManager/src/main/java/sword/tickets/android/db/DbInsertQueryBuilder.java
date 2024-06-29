package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.database.DbInsertQuery;
import sword.database.DbTable;

import static sword.tickets.android.db.PreconditionUtils.ensureNonNull;

public final class DbInsertQueryBuilder {

    @NonNull
    private final DbTable _table;

    @NonNull
    private final DbInsertQuery.Builder _builder;

    DbInsertQueryBuilder(@NonNull DbTable table) {
        ensureNonNull(table);
        _table = table;
        _builder = new DbInsertQuery.Builder(table);
    }

    @NonNull
    public DbInsertQueryBuilder put(int columnIndex, int value) {
        _builder.put(columnIndex, value);
        return this;
    }

    @NonNull
    public DbInsertQueryBuilder put(int columnIndex, String value) {
        _builder.put(columnIndex, value);
        return this;
    }

    @NonNull
    public DbInsertQueryBuilder put(int columnIndex, IdPutInterface id) {
        if (id != null) {
            id.put(columnIndex, _builder);
        }
        else if (_table.columns().get(columnIndex).isText()) {
            _builder.put(columnIndex, null);
        }
        else {
            _builder.put(columnIndex, 0);
        }

        return this;
    }

    @NonNull
    public DbInsertQuery build() {
        return _builder.build();
    }
}
