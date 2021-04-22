package me.xiaolei.room_lite.runtime.sqlite;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DataBaseProvider extends ContentProvider
{
    /**
     * 上下文
     */
    @SuppressLint("StaticFieldLeak")
    public static Context context;

    public static String authorities = "";

    /**
     * 保存所有的数据库实例，以及对应的数据库表的映射
     */
    private final static Map<String, LiteDataBase> liteDataBases = new HashMap<>();

    /**
     * 向映射库里注册数据库的实例
     */
    public static void offerLiteDataBase(String dbName, LiteDataBase dataBase)
    {
        liteDataBases.put(dbName, dataBase);
    }

    @Override
    public boolean onCreate()
    {
        context = getContext();
        String packageName = context.getPackageName();
        authorities = packageName + ".room_lite.provider";
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        String tableName = uri.getQueryParameter("tableName");
        if (tableName == null)
            return null;
        
        
        
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri)
    {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values)
    {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs)
    {
        return 0;
    }
}
