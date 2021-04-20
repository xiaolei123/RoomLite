package me.xiaolei.room_lite.runtime.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import me.xiaolei.room_lite.SQLiteReader;
import me.xiaolei.room_lite.WriterRunnable;
import me.xiaolei.room_lite.runtime.util.ThreadPoolFactory;

/**
 * 数据库操作类的包装类，主要致力于解决多线程并发读写的问题
 */
public abstract class SQLiteDatabaseWrapper extends SQLiteOpenHelper implements Closeable, SQLiteReader
{
    private SQLiteDatabase readableDataBase;
    private SQLiteDatabase writableDataBase;
    private Transaction transaction;

    // 写的线程池
    private final ExecutorService writeExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadPoolFactory("writeExecutor"));

    public SQLiteDatabaseWrapper(String dbPath, String name, int version)
    {
        super(new CustomContentWrap(InitProvider.context, dbPath), name, null, version);
    }

    @Override
    public void doTransaction(WriterRunnable runnable)
    {
        this.postWait((database) ->
        {
            database.beginTransaction();
            try
            {
                runnable.run(transaction);
                database.setTransactionSuccessful();
            } catch (Exception e)
            {
                e.printStackTrace();
            } finally
            {
                database.endTransaction();
            }
            return null;
        });
    }

    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs)
    {
        SQLiteDatabase database = this.getReadableSQLiteDatabase();
        return database.rawQuery(sql, selectionArgs);
    }

    @Override
    public int getVersion()
    {
        SQLiteDatabase database = this.getReadableSQLiteDatabase();
        return database.getVersion();
    }

    @Override
    public void setVersion(int version)
    {
        this.postWait((database) ->
        {
            database.setVersion(version);
            return null;
        });
    }

    @Override
    public void close()
    {
        writeExecutor.shutdownNow();
        super.close();
    }

    /**
     * 获取只读数据库
     */
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

    /**
     * 获取只写数据库
     */
    private SQLiteDatabase getWritableSQLiteDatabase()
    {
        if (writableDataBase == null)
        {
            synchronized (this)
            {
                if (writableDataBase == null)
                {
                    writableDataBase = this.getWritableDatabase();
                    transaction = new Transaction(writableDataBase);
                }
            }
        }
        return writableDataBase;
    }

    /**
     * 用同步的方式进行异步操作
     *
     * @param runnable 要执行的操作
     * @param <T>      返回的数据类型
     * @return 返回类型的数据
     */
    private <T> T postWait(LiteRunnable<T> runnable)
    {
        SQLiteDatabase database = this.getWritableSQLiteDatabase();
        Callable<T> callable = () -> runnable.run(database);
        FutureTask<T> task = new FutureTask<T>(callable);
        this.writeExecutor.submit(task);
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