package me.xiaolei.room_lite.runtime.sqlite;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * 可写的数据库
 */
public interface SQLiteWriter
{
    public void execSQL(String sql) throws SQLException;

    public void execSQL(String sql, Object[] bindArgs) throws SQLException;

    /**
     * Convenience method for inserting a row into the database.
     *
     * @param table             the table to insert the row into
     * @param values            this map contains the initial column values for the
     *                          row. The keys should be the column names and the values the
     *                          column values
     * @param conflictAlgorithm for insert conflict resolver. One of
     *                          {@link SQLiteDatabase#CONFLICT_NONE}, {@link SQLiteDatabase#CONFLICT_ROLLBACK},
     *                          {@link SQLiteDatabase#CONFLICT_ABORT}, {@link SQLiteDatabase#CONFLICT_FAIL},
     *                          {@link SQLiteDatabase#CONFLICT_IGNORE}, {@link SQLiteDatabase#CONFLICT_REPLACE}.
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     * @throws SQLException If the insert fails
     */
    public long insert(String table, int conflictAlgorithm, ContentValues values);

    public int delete(String table, String whereClause, String[] whereArgs);

    /**
     * Convenience method for updating rows in the database.
     *
     * @param table             the table to update in
     * @param conflictAlgorithm for update conflict resolver. One of
     *                          {@link SQLiteDatabase#CONFLICT_NONE}, {@link SQLiteDatabase#CONFLICT_ROLLBACK},
     *                          {@link SQLiteDatabase#CONFLICT_ABORT}, {@link SQLiteDatabase#CONFLICT_FAIL},
     *                          {@link SQLiteDatabase#CONFLICT_IGNORE}, {@link SQLiteDatabase#CONFLICT_REPLACE}.
     * @param values            a map from column names to new column values. null is a
     *                          valid value that will be translated to NULL.
     * @param whereClause       the optional WHERE clause to apply when updating.
     *                          Passing null will update all rows.
     * @param whereArgs         You may include ?s in the where clause, which
     *                          will be replaced by the values from whereArgs. The values
     *                          will be bound as Strings.
     * @return the number of rows affected
     */
    public int update(String table, int conflictAlgorithm, ContentValues values, String whereClause, String[] whereArgs);
}
