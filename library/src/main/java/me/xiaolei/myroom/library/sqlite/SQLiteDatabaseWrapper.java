package me.xiaolei.myroom.library.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据库操作类的包装类，主要致力于解决多线程并发读写的问题
 */
public abstract class SQLiteDatabaseWrapper extends SQLiteOpenHelper implements Closeable
{
    // 写的线程池
    private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();
    // 读的线程池
    private final ExecutorService readExecutor = Executors.newCachedThreadPool();

    public SQLiteDatabaseWrapper(String dbPath, String name, int version)
    {
        super(new CustomContentWrap(InitProvider.context, dbPath), name, null, version);
        // flag
        int flag = SQLiteDatabase.CREATE_IF_NECESSARY | SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING;
    }

    public void execSQL(String sql) throws SQLException
    {
        postWait(writeExecutor, (database) ->
        {
            database.execSQL(sql);
            return null;
        });
        System.out.println(123);
    }

    public void execSQL(String sql, Object[] bindArgs) throws SQLException
    {
        postWait(writeExecutor, (database) ->
        {
            database.execSQL(sql, bindArgs);
            return null;
        });
    }

    public long insert(String table, String nullColumnHack, ContentValues values)
    {
        return postWait(writeExecutor, (database) -> database.insert(table, nullColumnHack, values));
    }

    public int delete(String table, String whereClause, String[] whereArgs)
    {
        return postWait(writeExecutor, (database) -> database.delete(table, whereClause, whereArgs));
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs)
    {
        return postWait(writeExecutor, (database) -> database.update(table, values, whereClause, whereArgs));
    }

    public Cursor rawQuery(String sql, String[] selectionArgs)
    {
        return postWait(readExecutor, (database) -> database.rawQuery(sql, selectionArgs));
    }

    public void beginTransaction()
    {
        postWait(writeExecutor, (database) ->
        {
            database.beginTransaction();
            return null;
        });
    }

    public void setTransactionSuccessful()
    {
        postWait(writeExecutor, (database) ->
        {
            database.setTransactionSuccessful();
            return null;
        });
    }

    public void endTransaction()
    {
        postWait(writeExecutor, (database) ->
        {
            database.endTransaction();
            return null;
        });
    }

    public int getVersion()
    {
        return postWait(readExecutor, SQLiteDatabase::getVersion);
    }

    public void setVersion(int version)
    {
        postWait(writeExecutor, (database) ->
        {
            database.setVersion(version);
            return null;
        });
    }

    @Override
    public void close()
    {
        readExecutor.shutdownNow();
        writeExecutor.shutdownNow();
        super.close();
    }

    private SQLiteDatabase readableDataBase;
    private SQLiteDatabase writableDataBase;

    private SQLiteDatabase getReadableSQLiteDatabase()
    {
        if (readableDataBase == null)
        {
            synchronized (this)
            {
                if (readableDataBase == null)
                    readableDataBase = this.getReadableDatabase();
            }
        }
        return readableDataBase;
    }

    private SQLiteDatabase getWritableSQLiteDatabase()
    {
        if (writableDataBase == null)
        {
            synchronized (this)
            {
                if (writableDataBase == null)
                    writableDataBase = this.getWritableDatabase();
            }
        }
        return writableDataBase;
    }

    /**
     * 用同步的方式进行异步操作
     *
     * @param executor 要执行的线程池
     * @param runnable 要执行的操作
     * @param <T>      返回的数据类型
     * @return 返回类型的数据
     */
    private <T> T postWait(ExecutorService executor, LiteRunnable<T> runnable)
    {
        SQLiteDatabase database = executor == readExecutor ? this.getReadableSQLiteDatabase() : this.getWritableSQLiteDatabase();
        Callable<T> callable = () ->
        {
            System.out.println(Thread.currentThread());
            return runnable.run(database);
        };
        FutureTask<T> task = new FutureTask<T>(callable);
        executor.submit(task);
        try
        {
            return task.get(/*30, TimeUnit.SECONDS*/);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private interface LiteRunnable<T>
    {
        T run(SQLiteDatabase database);
    }
}