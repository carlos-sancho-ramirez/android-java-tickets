package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.database.DbQuery;
import sword.database.DbTable;

import static sword.tickets.android.db.PreconditionUtils.ensureNonNull;

final class DbQueryBuilder {

    @NonNull
    private final DbTable _table;

    @NonNull
    private final DbQuery.Builder _builder;

    DbQueryBuilder(@NonNull DbTable table) {
        ensureNonNull(table);
        _table = table;
        _builder = new DbQuery.Builder(table);
    }

    DbQueryBuilder where(int columnIndex, int value) {
        _builder.where(columnIndex, value);
        return this;
    }

    DbQueryBuilder where(int columnIndex, @NonNull IntEnumValue value) {
        _builder.where(columnIndex, value.value());
        return this;
    }

    DbQueryBuilder where(int columnIndex, IdWhereInterface id) {
        if (id != null) {
            id.where(columnIndex, _builder);
        }
        else if (_table.columns().get(columnIndex).isText()) {
            _builder.where(columnIndex, (String) null);
        }
        else {
            _builder.where(columnIndex, 0);
        }

        return this;
    }

    @NonNull
    public DbQueryBuilder orderBy(int... columnIndexes) {
        _builder.orderBy(columnIndexes);
        return this;
    }

    @NonNull
    DbQueryBuilder orderBy(DbQuery.Ordered... ordering) {
        _builder.orderBy(ordering);
        return this;
    }

    @NonNull
    DbQuery select(int... selection) {
        return _builder.select(selection);
    }
}
