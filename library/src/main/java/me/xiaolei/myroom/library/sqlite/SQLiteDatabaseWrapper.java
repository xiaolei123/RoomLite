package me.xiaolei.myroom.library.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import me.xiaolei.myroom.library.util.ThreadPoolFactory;

/**
 * 数据库操作类的包装类，主要致力于解决多线程并发读写的问题
 */
public abstract class SQLiteDatabaseWrapper extends SQLiteOpenHelper implements Closeable
{
    private SQLiteDatabase readableDataBase;
    private SQLiteDatabase writableDataBase;
    private Transaction transaction;

    // 写的线程池
    private final ExecutorService writeExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadPoolFactory("writeExecutor"));
    // 读的线程池
    private final ExecutorService readExecutor = Executors.newCachedThreadPool(new ThreadPoolFactory("readExecutor"));

    public SQLiteDatabaseWrapper(String dbPath, String name, int version)
    {
        super(new CustomContentWrap(InitProvider.context, dbPath), name, null, version);
    }

    public void doTransaction(Transaction.TransactionRunnable runnable)
    {
        postWait(writeExecutor, (database) ->
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

    public Cursor rawQuery(String sql, String[] selectionArgs)
    {
        return postWait(readExecutor, (database) -> database.rawQuery(sql, selectionArgs));
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
     * @param executor 要执行的线程池
     * @param runnable 要执行的操作
     * @param <T>      返回的数据类型
     * @return 返回类型的数据
     */
    private <T> T postWait(ExecutorService executor, LiteRunnable<T> runnable)
    {
        SQLiteDatabase database = executor == readExecutor ? this.getReadableSQLiteDatabase() : this.getWritableSQLiteDatabase();
        Callable<T> callable = () -> runnable.run(database);
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