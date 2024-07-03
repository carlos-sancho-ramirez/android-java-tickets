package sword.tickets.android.db;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import sword.database.DbIdentifiableQueryBuilder;
import sword.database.DbSettableQueryBuilder;
import sword.tickets.android.PreferenceIdInterface;

import static sword.tickets.android.PreconditionUtils.ensureValidArguments;

public final class ProjectId implements IdInterface, PreferenceIdInterface {
    final int key;

    public ProjectId(int key) {
        ensureValidArguments(key != 0);
        this.key = key;
    }

    @Override
    public int hashCode() {
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (!(obj instanceof ProjectId)) {
            return false;
        }

        final ProjectId that = (ProjectId) obj;
        return key == that.key;
    }

    @NonNull
    @Override
    public String toString() {
        return "ProjectId(" + key + ")";
    }

    @Override
    public void where(int columnIndex, @NonNull DbIdentifiableQueryBuilder builder) {
        builder.where(columnIndex, key);
    }

    @Override
    public void put(int columnIndex, @NonNull DbSettableQueryBuilder builder) {
        builder.put(columnIndex, key);
    }

    @Override
    public void put(@NonNull SharedPreferences.Editor editor, String preferenceName) {
        editor.putInt(preferenceName, key);
    }
}
