package sword.tickets.android.db;

import sword.database.DbQuery;
import sword.database.DbTable;

import androidx.annotation.NonNull;

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
    DbQuery select(int... selection) {
        return _builder.select(selection);
    }
}
