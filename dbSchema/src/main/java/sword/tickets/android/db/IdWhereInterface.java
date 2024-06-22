package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.database.DbIdentifiableQueryBuilder;

public interface IdWhereInterface {
    void where(int columnIndex, @NonNull DbIdentifiableQueryBuilder builder);
}
