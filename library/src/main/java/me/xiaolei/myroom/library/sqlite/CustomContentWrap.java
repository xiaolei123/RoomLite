package me.xiaolei.myroom.library.sqlite;

import android.content.Context;
import android.content.ContextWrapper;

import java.io.File;

/**
 * 用于自定义路径的数据库Context包装类
 */
public class CustomContentWrap extends ContextWrapper
{
    private final String dbPath;

    public CustomContentWrap(Context base, String dbPath)
    {
        super(base);
        this.dbPath = dbPath;
    }

    @Override
    public File getDatabasePath(String name)
    {
        return new File(dbPath, name);
    }
}
