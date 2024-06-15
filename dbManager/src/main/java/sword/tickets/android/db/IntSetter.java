package sword.tickets.android.db;

import androidx.annotation.NonNull;

import sword.database.DbValue;

public interface IntSetter<T> {
    T getKeyFromInt(int key);
    T getKeyFromDbValue(@NonNull DbValue value);
}
