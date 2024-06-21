package sword.tickets.android.db;

import sword.database.DbIdentifiableQueryBuilder;

public interface IdWhereInterface {
    void where(int columnIndex, DbIdentifiableQueryBuilder builder);
}
