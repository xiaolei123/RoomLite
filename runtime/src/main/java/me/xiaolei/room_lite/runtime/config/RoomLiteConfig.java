package me.xiaolei.room_lite.runtime.config;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import me.xiaolei.room_lite.runtime.entity.EntityHelper;

public class RoomLiteConfig
{
    private static final String suffix = ".room_lite";
    private final Context context;
    private final String dbName;
    private final String packageName;
    private static final String CREATE_SQL_KEY = "CREATE_SQL_KEY";
    private static final String COLUMN_NAMES_KEY = "COLUMN_NAMES_KEY";
    private static final String INDEX_NAMES_KEY = "INDEX_NAMES_KEY";

    public RoomLiteConfig(Context context, String dbName)
    {
        this.context = context;
        this.dbName = dbName;
        this.packageName = context.getPackageName();
    }

    public void saveOrUpdateEntityMsg(EntityHelper helper)
    {
        String tableName = helper.getTableName();
        String spName = packageName + "." + dbName + "." + tableName + "." + suffix;
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        // 创建语句
        String createSql = helper.getCreateSQL();
        // 所有的字段名称的集合
        Set<String> columnNames = new HashSet<>(Arrays.asList(helper.columnNames()));
        // 所有索引名称的集合
        Set<String> indexNames = new HashSet<>(Arrays.asList(helper.indexNames()));
        // 保存起来
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(CREATE_SQL_KEY, createSql);
        editor.putStringSet(COLUMN_NAMES_KEY, columnNames);
        editor.putStringSet(INDEX_NAMES_KEY, indexNames);
        editor.apply();
        editor.commit();
    }

    public boolean isSame(EntityHelper helper)
    {
        String tableName = helper.getTableName();
        String spName = packageName + "." + dbName + "." + tableName + "." + suffix;
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        // 创建语句
        String createSql = helper.getCreateSQL();
        // 所有的字段名称的集合
        Set<String> columnNames = new HashSet<>(Arrays.asList(helper.columnNames()));
        // 所有索引名称的集合
        Set<String> indexNames = new HashSet<>(Arrays.asList(helper.indexNames()));
        // 从缓存中取出
        String inner_createSql = sp.getString(CREATE_SQL_KEY, "");
        Set<String> inner_columnNames = sp.getStringSet(COLUMN_NAMES_KEY, new HashSet<>());
        Set<String> inner_indexNames = sp.getStringSet(INDEX_NAMES_KEY, new HashSet<>());
        // 进行比较
        boolean sameCreate = inner_createSql.equals(createSql);
        boolean sameColumn = inner_columnNames.equals(columnNames);
        boolean sameIndex = inner_indexNames.equals(indexNames);
        return sameCreate && sameColumn && sameIndex;
    }


    private String md5(String str)
    {
        try
        {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(str.getBytes());
            byte[] digest = m.digest();
            StringBuilder resultBuilder = new StringBuilder();
            for (byte b : digest)
            {
                String hexStr = Integer.toHexString((0x000000FF & b) | 0xFFFFFF00).substring(6);
                resultBuilder.append(hexStr);
            }
            return resultBuilder.toString();
        } catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
}
