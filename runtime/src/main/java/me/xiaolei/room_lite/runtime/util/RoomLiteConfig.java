package me.xiaolei.room_lite.runtime.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

public class RoomLiteConfig
{
    private final SharedPreferences sp;

    public RoomLiteConfig(Context context, String dbName)
    {
        String packageName = context.getPackageName();
        String suffix = ".room_lite";
        String spName = packageName + "." + dbName + suffix;
        sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    public void saveTableSQLMessage(String tableName, String createSql, String[] indexSQLs)
    {
        // 表名的MD5
        String tableMd5 = this.md5(tableName);
        // 创建表的MD5
        String createMd5 = this.md5(createSql);
        // 索引名称的MD5
        String indexMd5 = this.md5(tableMd5 + "_index");
        // 所有的索引的MD5的集合
        Set<String> indexMd5s = new HashSet<>();
        for (String indexSQL : indexSQLs)
        {
            indexMd5s.add(this.md5(indexSQL));
        }

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(tableMd5, createMd5);
        editor.putStringSet(indexMd5, indexMd5s);
        editor.apply();
        editor.commit();
    }

    public boolean isSame(String tableName, String createSql, String[] indexSQLs)
    {
        // 表名的MD5
        String tableMd5 = this.md5(tableName);
        // 创建表的MD5
        String createMd5 = this.md5(createSql);
        // 索引名称的MD5
        String indexMd5 = this.md5(tableMd5 + "_index");
        // 所有的索引的MD5的集合
        Set<String> indexMd5s = new HashSet<>();
        for (String indexSQL : indexSQLs)
        {
            indexMd5s.add(this.md5(indexSQL));
        }
        // 从缓存中取出
        String inner_createMd5 = sp.getString(tableMd5, "");
        Set<String> inner_indexMd5s = sp.getStringSet(indexMd5, new HashSet<>());
        
        boolean sameCreate = inner_createMd5.equals(createMd5);
        boolean sameIndex = inner_indexMd5s.equals(indexMd5s);
        
        return sameCreate && sameIndex;
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
