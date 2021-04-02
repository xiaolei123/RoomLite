package me.xiaolei.myroom.library.sqlite;

import android.database.sqlite.SQLiteDatabase;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import me.xiaolei.myroom.library.sqlite.calls.OnResult;
import me.xiaolei.myroom.library.sqlite.calls.PostRunnable;
import me.xiaolei.myroom.library.sqlite.calls.ResultRunnable;

/**
 * 对数据库的一层包装
 */
public class BaseDatabase implements Closeable
{
    private final SQLiteDatabase database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BaseDatabase(RoomLiteDatabase liteDatabase)
    {
        String dbPath = new File(liteDatabase.getDatabaseDir(), liteDatabase.getDatabaseName()).getAbsolutePath();
        database = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        this.post((database) -> this.onInit(liteDatabase, database));
    }

    /**
     * 初始化
     */
    private void onInit(RoomLiteDatabase liteDatabase, SQLiteDatabase database)
    {
        // ---------------------invoke onOpen Function---------------------------
        database.beginTransaction();
        try
        {
            liteDatabase.onOpen(database);
            database.setTransactionSuccessful();
        } finally
        {
            database.endTransaction();
        }
        // ----------------------check the database version--------------------------
        final int version = database.getVersion();
        final int mNewVersion = liteDatabase.version();
        if (version != mNewVersion)
        {
            database.beginTransaction();
            try
            {
                if (version > mNewVersion)
                {
                    liteDatabase.onDowngrade(database, version, mNewVersion);
                } else
                {
                    liteDatabase.onUpgrade(database, version, mNewVersion);
                }
                database.setVersion(mNewVersion);
                database.setTransactionSuccessful();
            } finally
            {
                database.endTransaction();
            }
        }
    }

    /**
     * 阻塞执行，不关心结果
     */
    public void await(PostRunnable runnable)
    {
        Callable<Void> callable = () ->
        {
            runnable.run(database);
            return null;
        };
        FutureTask<Void> task = new FutureTask<>(callable);
        executor.submit(task);
        try
        {
            task.get(/*30, TimeUnit.SECONDS*/);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 阻塞执行，还关心结果
     */
    public <T> T await(ResultRunnable<T> runnable)
    {
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

    /**
     * 异步执行，不需要结果
     */
    public void post(PostRunnable runnable)
    {
        Runnable callable = () -> runnable.run(database);
        executor.submit(callable);
    }

    /**
     * 异步执行，还需要结果回调
     */
    public <T> void post(ResultRunnable<T> runnable, OnResult<T> result)
    {
        Runnable callable = () ->
        {
            result.callBack(runnable.run(database));
        };
        executor.submit(callable);
    }

    @Override
    public void close() throws IOException
    {
        executor.shutdownNow();
        this.database.close();
    }
}