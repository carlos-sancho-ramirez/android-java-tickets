package sword.tickets.android.db;

import sword.database.DbDeleteQuery;
import sword.database.DbTable;

import androidx.annotation.NonNull;

public final class DbDeleteQueryBuilder {

    @NonNull
    private final DbDeleteQuery.Builder _builder;

    DbDeleteQueryBuilder(@NonNull DbTable table) {
        _builder = new DbDeleteQuery.Builder(table);
    }

    @NonNull
    public DbDeleteQueryBuilder where(int columnIndex, int value) {
        _builder.where(columnIndex, value);
        return this;
    }

    @NonNull
    public DbDeleteQueryBuilder where(int columnIndex, @NonNull IdWhereInterface id) {
        id.where(columnIndex, _builder);
        return this;
    }

    @NonNull
    public DbDeleteQuery build() {
        return _builder.build();
    }
}
