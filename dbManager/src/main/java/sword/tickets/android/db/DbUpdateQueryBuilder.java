package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.database.DbTable;
import sword.database.DbUpdateQuery;

import static sword.tickets.android.db.PreconditionUtils.ensureNonNull;

public final class DbUpdateQueryBuilder {

    @NonNull
    private final DbTable _table;

    @NonNull
    private final DbUpdateQuery.Builder _builder;

    DbUpdateQueryBuilder(@NonNull DbTable table) {
        ensureNonNull(table);
        _table = table;
        _builder = new DbUpdateQuery.Builder(table);
    }

    @NonNull
    public DbUpdateQueryBuilder where(int columnIndex, int value) {
        _builder.where(columnIndex, value);
        return this;
    }

    @NonNull
    public DbUpdateQueryBuilder where(int columnIndex, @NonNull IdWhereInterface id) {
        id.where(columnIndex, _builder);
        return this;
    }

    @NonNull
    public DbUpdateQueryBuilder put(int columnIndex, int value) {
        _builder.put(columnIndex, value);
        return this;
    }

    @NonNull
    public DbUpdateQueryBuilder put(int columnIndex, String value) {
        _builder.put(columnIndex, value);
        return this;
    }

    @NonNull
    public DbUpdateQueryBuilder put(int columnIndex, IdPutInterface id) {
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
    public DbUpdateQuery build() {
        return _builder.build();
    }
}
