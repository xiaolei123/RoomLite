package me.xiaolei.myroom.library.dao_proxy.proxy;

import android.database.Cursor;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import me.xiaolei.myroom.library.adapters.Adapters;
import me.xiaolei.myroom.library.adapters.ContainerAdapter;
import me.xiaolei.myroom.library.anno.dao.Query;
import me.xiaolei.myroom.library.coverts.Convert;
import me.xiaolei.myroom.library.coverts.Converts;
import me.xiaolei.myroom.library.dao_proxy.DaoProxy;
import me.xiaolei.myroom.library.sqlite.BaseDatabase;
import me.xiaolei.myroom.library.sqlite.RoomLiteDatabase;
import me.xiaolei.myroom.library.sqlite.calls.LiteRunnable;
import me.xiaolei.myroom.library.util.QueryUtil;
import me.xiaolei.myroom.library.util.RoomLiteUtil;

public class QueryProxy extends DaoProxy
{
    public QueryProxy(RoomLiteDatabase liteDatabase, BaseDatabase database)
    {
        super(liteDatabase, database);
    }

    public Object invoke(Method method, Object[] args)
    {
        Query query = method.getAnnotation(Query.class);
        if (query == null)
            throw new RuntimeException(method + " 必须使用@Query 注解");
        // 获取当前表的类型
        Class<?> tableKlass = query.entity();
        // 获取当前数据库所有的表的实体类
        List<Class<?>> entities = Arrays.asList(liteDatabase.getEntities());
        if (!entities.contains(tableKlass))
            throw new RuntimeException(method + " 的@Query.entity 所指定的Entity不在数据库[" + liteDatabase.getDatabaseName() + "]内");
        // 提取出返回类型
        Class<?> returnType = method.getReturnType();
        // SQL语句的构建
        StringBuilder querySQLBuilder = new StringBuilder();
        // 获取表名
        String tableName = RoomLiteUtil.getTableName(tableKlass);
        // 查询什么
        String what = query.what();
        // 查询条件是什么
        String where = query.whereClause();
        // 取区间
        String limit = query.limit();
        // 构建查询语句
        querySQLBuilder.append("SELECT ")
                .append(what)
                .append(" FROM ")
                .append(tableName)
                .append(" WHERE ")
                .append(where)
                .append(" LIMIT ")
                .append(limit);
        // 问号和参数数量对不上
        if (args != null && this.argCount(where) != args.length)
        {
            throw new RuntimeException(method + " where 里问号的数量跟参数数量不符");
        }
        // 构建查询语句的参数
        final String[] whereArgs;
        if (args != null && args.length > 0 && this.argCount(where) > 0)
        {
            whereArgs = new String[args.length];
            for (int i = 0; i < args.length; i++)
            {
                Object arg = args[i];
                whereArgs[i] = String.valueOf(arg);
            }
        } else
        {
            whereArgs = null;
        }
        // 首先检查，是不是基本类型，或者是用Convert添加的支持类型，再或者是当前查询的表的类型
        Convert convert = Converts.getConvert(returnType);
        if (convert != null || returnType == tableKlass)
        {
            LiteRunnable<Object> singleRunnable = database ->
            {
                try (Cursor cursor = database.rawQuery(querySQLBuilder.toString(), whereArgs))
                {
                    if (!checkSupportSingle(cursor, returnType))
                        throw new RuntimeException(returnType + " 返回类型的参数不支持");
                    if (cursor.moveToNext())
                        return QueryUtil.parseObject(cursor, returnType);
                    else
                        return RoomLiteUtil.defaultValue(returnType);
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            };
            return database.postWait(singleRunnable);
        } else if (returnType.isArray()) // 是数组
        {
            Class<?> comType = returnType.getComponentType();
            Convert comConvert = Converts.getConvert(comType);
            if (comConvert == null && comType != tableKlass)
                throw new RuntimeException("数组类型:" + comType + " 不受支持");
            LiteRunnable<Object> arrayRunnable = database ->
            {
                try (Cursor cursor = database.rawQuery(querySQLBuilder.toString(), whereArgs))
                {
                    Object[] array = (Object[]) Array.newInstance(comType, cursor.getCount());
                    while (cursor.moveToNext())
                    {
                        array[cursor.getPosition()] = QueryUtil.parseObject(cursor, comType);
                    }
                    return array;
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            };
            return database.postWait(arrayRunnable);
        } else
        {
            ContainerAdapter<?> adapter = Adapters.getAdapter(returnType);
            if (adapter == null)
                throw new RuntimeException(returnType + " 类型不受支持");
            Type genericType = getGenericType(method);
            return adapter.newInstance(database, genericType, querySQLBuilder.toString(), whereArgs);
        }
    }


    public static boolean checkSupportSingle(Cursor cursor, Class<?> klass)
    {
        int columnCount = cursor.getColumnCount();
        int count = cursor.getCount();
        if (Converts.hasConvert(klass)) // 基本类型的单个对象
        {
            if (count > 1 || columnCount > 1)
                return false;
        } else // 表的类型的单个对象
        {
            if (count > 1)
                return false;
        }
        return true;
    }

    /**
     * 获取函数返回List泛型的真实类型
     */
    private Type getGenericType(Method method)
    {
        Type genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType))
            throw new RuntimeException(method + " 返回类型<>里必须写上泛型");
        ParameterizedType type = (ParameterizedType) genericReturnType;
        return type.getActualTypeArguments()[0];
    }

    /**
     * 获取一个where语句中，有几个问号
     *
     * @param where
     */
    private int argCount(String where)
    {
        int count = 0;
        char[] chars = where.toCharArray();
        for (char c : chars)
        {
            if (c == '?')
                count++;
        }
        return count;
    }
}
