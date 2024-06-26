package sword.tickets.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import sword.collections.AbstractTransformer;
import sword.collections.Function;
import sword.collections.ImmutableIntKeyMap;
import sword.collections.ImmutableList;
import sword.collections.IntKeyMap;
import sword.collections.List;
import sword.collections.MutableList;
import sword.database.Database;
import sword.database.DbColumn;
import sword.database.DbDeleteQuery;
import sword.database.DbIndex;
import sword.database.DbInsertQuery;
import sword.database.DbQuery;
import sword.database.DbResult;
import sword.database.DbTable;
import sword.database.DbUpdateQuery;
import sword.database.DbValue;
import sword.tickets.android.annotation.TestSwitcher;
import sword.tickets.android.db.TicketsDbManagerImpl;
import sword.tickets.android.db.TicketsDbSchema;
import sword.tickets.android.sqlite.SQLiteDbQuery;

import static sword.langbook3.android.sqlite.SqliteUtils.sqlType;
import static sword.tickets.android.PreconditionUtils.ensureValidArguments;
import static sword.tickets.android.PreconditionUtils.ensureValidState;
import static sword.tickets.android.PreconditionUtils.ensureNonNull;

public final class DbManager extends SQLiteOpenHelper {

    private static final String DB_NAME = "Tickets";
    private static DbManager _instance;

    @NonNull
    public static DbManager getInstance() {
        ensureValidState(_instance != null);
        return _instance;
    }

    static void createInstance(@NonNull Context context) {
        ensureValidState(_instance == null);
        _instance = new DbManager(context);
    }

    @TestSwitcher
    Database _database;
    private TicketsDbManagerImpl _ticketsManager;

    private DbManager(@NonNull Context context) {
        super(context, DB_NAME, null, TicketsDbSchema.getInstance().currentSchemaVersionCode());
    }

    @NonNull
    public Database getDatabase() {
        if (_database == null) {
            _database = new ManagerDatabase();
        }

        return _database;
    }

    @NonNull
    public TicketsDbManagerImpl getManager() {
        if (_ticketsManager == null) {
            _ticketsManager = new TicketsDbManagerImpl(getDatabase());
        }

        return _ticketsManager;
    }

    private static void createTable(@NonNull SQLiteDatabase db, @NonNull DbTable table) {
        final StringBuilder builder = new StringBuilder()
                .append("CREATE TABLE ")
                .append(table.name())
                .append(" (");

        final Function<DbColumn, String> mapFunc = column -> column.name() + ' ' + sqlType(column);
        final String columns = table.columns().map(mapFunc)
                .reduce((left, right) -> left + ", " + right);

        builder.append(columns).append(')');
        db.execSQL(builder.toString());
    }

    private static void createIndex(@NonNull SQLiteDatabase db, @NonNull DbIndex index) {
        final DbTable table = index.table;
        final String tableName = table.name();
        final String columnName = table.columns().get(index.column).name();
        db.execSQL("CREATE INDEX I" + tableName + 'C' + columnName + " ON " + tableName + " (" + columnName + ')');
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final TicketsDbSchema schema = TicketsDbSchema.getInstance();
        for (DbTable table : schema.tables()) {
            createTable(db, table);
        }

        for (DbIndex index : schema.indexes()) {
            createIndex(db, index);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        throw new UnsupportedOperationException("Currently there is only one version!");
    }

    private static final class SQLiteDbIntValue implements DbValue {

        int _value;

        @Override
        public boolean isText() {
            return false;
        }

        @Override
        public int toInt() throws UnsupportedOperationException {
            return _value;
        }

        @Override
        public String toText() {
            return Integer.toString(_value);
        }
    }

    private static final class SQLiteDbTextValue implements DbValue {

        String _value;

        @Override
        public boolean isText() {
            return true;
        }

        @Override
        public int toInt() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("String column should not be converted to integer");
        }

        @Override
        public String toText() {
            return _value;
        }
    }

    private static final class SQLiteDbResult extends AbstractTransformer<List<DbValue>> implements DbResult {

        @NonNull
        private final ImmutableList<DbColumn> _columns;

        @NonNull
        private final Cursor _cursor;

        private final int _rowCount;
        private int _nextRowIndex;
        private MutableList<DbValue> _rowHolder;

        SQLiteDbResult(@NonNull ImmutableList<DbColumn> columns, @NonNull Cursor cursor) {
            ensureValidArguments(!columns.isEmpty());
            ensureNonNull(cursor);
            _columns = columns;
            _cursor = cursor;
            _rowCount = cursor.getCount();

            if (!cursor.moveToFirst()) {
                close();
            }
        }

        @Override
        public void close() {
            _nextRowIndex = _rowCount;
            _cursor.close();
        }

        @Override
        public int getRemainingRows() {
            return _rowCount - _nextRowIndex;
        }

        @Override
        public boolean hasNext() {
            return _nextRowIndex < _rowCount;
        }

        private DbValue newDbValueFromCursor(int index) {
            if (_columns.get(index).isText()) {
                final SQLiteDbTextValue holder = new SQLiteDbTextValue();
                holder._value = _cursor.getString(index);
                return holder;
            }
            else {
                final SQLiteDbIntValue holder = new SQLiteDbIntValue();
                holder._value = _cursor.getInt(index);
                return holder;
            }
        }

        private void reuseDbValue(int index) {
            final DbValue recyclable = _rowHolder.get(index);
            if (_columns.get(index).isText()) {
                final SQLiteDbTextValue holder = (SQLiteDbTextValue) recyclable;
                holder._value = _cursor.getString(index);
            }
            else {
                final SQLiteDbIntValue holder = (SQLiteDbIntValue) recyclable;
                holder._value = _cursor.getInt(index);
            }
        }

        @Override
        public List<DbValue> next() {
            if (!hasNext()) {
                throw new UnsupportedOperationException("End already reached");
            }

            final int columnCount = _columns.size();
            if (_rowHolder == null) {
                _rowHolder = MutableList.empty((currentLength, newSize) -> columnCount);
                for (int i = 0; i < columnCount; i++) {
                    _rowHolder.append(newDbValueFromCursor(i));
                }
            }
            else {
                for (int i = 0; i < columnCount; i++) {
                    reuseDbValue(i);
                }
            }

            if (!_cursor.moveToNext()) {
                close();
            }
            _nextRowIndex++;

            return _rowHolder;
        }
    }

    final class ManagerDatabase implements Database {

        int _dbWriteVersion = 1;

        @Override
        public boolean update(DbUpdateQuery query) {
            ++_dbWriteVersion;
            final ImmutableIntKeyMap<DbValue> constraints = query.constraints();
            final ImmutableList<DbColumn> columns = query.table().columns();
            final String whereClause = constraints.keySet()
                    .map(key -> columns.get(key).name() + "=?")
                    .reduce((a, b) -> a + " AND " + b);
            final int constraintsSize = constraints.size();
            final String[] values = new String[constraintsSize];
            for (int i = 0; i < constraintsSize; i++) {
                final DbValue value = constraints.valueAt(i);
                values[i] = value.isText()? value.toText() : Integer.toString(value.toInt());
            }

            final ContentValues cv = new ContentValues();
            for (IntKeyMap.Entry<DbValue> entry : query.values().entries()) {
                final String columnName = query.table().columns().get(entry.key()).name();
                final DbValue value = entry.value();
                if (value.isText()) {
                    cv.put(columnName, value.toText());
                }
                else {
                    cv.put(columnName, value.toInt());
                }
            }

            return getWritableDatabase().update(query.table().name(), cv, whereClause, values) > 0;
        }

        @Override
        public DbResult select(DbQuery query) {
            return new SQLiteDbResult(query.columns(), getReadableDatabase().rawQuery(new SQLiteDbQuery(query).toSql(), null));
        }

        @Override
        public Integer insert(DbInsertQuery query) {
            ++_dbWriteVersion;
            final int count = query.getColumnCount();
            final ContentValues cv = new ContentValues();
            for (int i = 0; i < count; i++) {
                final String name = query.getColumn(i).name();
                final DbValue value = query.getValue(i);
                if (value.isText()) {
                    cv.put(name, value.toText());
                }
                else {
                    cv.put(name, value.toInt());
                }
            }

            final long returnId = getWritableDatabase().insert(query.getTable().name(), null, cv);
            return (returnId >= 0)? (int) returnId : null;

        }

        @Override
        public boolean delete(DbDeleteQuery query) {
            ++_dbWriteVersion;
            final ImmutableIntKeyMap<DbValue> constraints = query.constraints();
            final ImmutableList<DbColumn> columns = query.table().columns();
            final String whereClause = constraints.keySet()
                    .map(key -> columns.get(key).name() + "=?")
                    .reduce((a, b) -> a + " AND " + b);

            final int constraintsSize = constraints.size();
            final String[] values = new String[constraintsSize];
            for (int i = 0; i < constraintsSize; i++) {
                final DbValue value = constraints.valueAt(i);
                values[i] = value.isText()? value.toText() : Integer.toString(value.toInt());
            }

            return getWritableDatabase().delete(query.table().name(), whereClause, values) > 0;
        }
    }
}
