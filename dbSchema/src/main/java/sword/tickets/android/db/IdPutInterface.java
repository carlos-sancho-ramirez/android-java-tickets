package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.database.DbSettableQueryBuilder;

public interface IdPutInterface {
    void put(int columnIndex, @NonNull DbSettableQueryBuilder builder);
}
