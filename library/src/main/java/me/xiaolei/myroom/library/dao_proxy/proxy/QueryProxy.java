package me.xiaolei.myroom.library.dao_proxy.proxy;

import android.database.Cursor;
import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import me.xiaolei.myroom.library.anno.dao.Query;
import me.xiaolei.myroom.library.coverts.Convert;
import me.xiaolei.myroom.library.coverts.Converts;
import me.xiaolei.myroom.library.dao_proxy.DaoProxy;
import me.xiaolei.myroom.library.sqlite.BaseDatabase;
import me.xiaolei.myroom.library.sqlite.RoomLiteDatabase;
import me.xiaolei.myroom.library.sqlite.calls.LiteRunnable;
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
        // 返回数据的类型
        ReturnCount returnCount;
        // 获取需要解析的类型
        Class<?> type;
        if (returnType == List.class) // 如果是List 则获取List<>的泛型类型
        {
            type = this.getListGenericType(method);
            returnCount = ReturnCount.LIST;
        } else if (returnType.isArray()) // 如果是数组，则获取数组成员的类型
        {
            type = returnType.getComponentType();
            returnCount = ReturnCount.ARRAY;
        } else // 则直接就是返回类型
        {
            type = returnType;
            returnCount = ReturnCount.SINGLE;
        }
        // 判断获取到的type，是不是被支持
        if (!Converts.hasConvert(type) && type != tableKlass)
            throw new RuntimeException(method + " 返回的类型" + type + "，不在允许范围内");
        Log.e("RoomLite", querySQLBuilder + "," + Arrays.toString(whereArgs));
        
        LiteRunnable<Object> runnable = (database) ->
        {
            try (Cursor cursor = database.rawQuery(querySQLBuilder.toString(), whereArgs))
            {
                int columnCount = cursor.getColumnCount();
                int count = cursor.getCount();
                if (returnCount == ReturnCount.SINGLE) // 返回单个对象
                {
                    if (Converts.hasConvert(type)) // 基本类型的单个对象
                    {
                        if (count > 1 || columnCount > 1)
                            throw new RuntimeException(method + " 返回类型:" + type + " 对应的查询结果列:" + columnCount + " 行:" + count + " 不匹配");
                    } else // 表的类型的单个对象
                    {
                        if (count > 1)
                            throw new RuntimeException(method + " 返回类型:" + type + " 对应的查询结果行: " + count + " 不匹配");
                    }
                    if (cursor.moveToNext())
                        return parseObject(cursor, type);
                    else
                        return RoomLiteUtil.defaultValue(type);
                } else if (returnCount == ReturnCount.ARRAY) // 返回数组
                {
                    Object[] array = (Object[]) Array.newInstance(type, count);
                    while (cursor.moveToNext())
                    {
                        array[cursor.getPosition()] = parseObject(cursor, type);
                    }
                    return array;
                } else// 返回List
                {
                    List<Object> list = new LinkedList<>();
                    while (cursor.moveToNext())
                    {
                        list.add(parseObject(cursor, type));
                    }
                    return list;
                }
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        };
        return database.postWait(runnable);
    }

    /**
     * 返回数据的类型
     */
    private enum ReturnCount
    {
        SINGLE, // 单个对象
        ARRAY,  // 数组
        LIST    // 列表
    }

    /**
     * 将查询的结果，转换成type所对应的类型对象
     */
    private Object parseObject(Cursor cursor, Class<?> type)
    {
        String[] columnNames = cursor.getColumnNames();
        if (Converts.hasConvert(type)) // 解析基本类型
        {
            return parseObjectByName(cursor, columnNames[0], type);
        } else // 解析自定义类型
        {
            try
            {
                List<Field> fields = RoomLiteUtil.getFields(type);
                Object cusObj = type.newInstance();
                for (String columnName : columnNames)
                {
                    Field field = null;
                    for (Field f : fields)
                    {
                        if (columnName.equals(RoomLiteUtil.getColumnName(f)))
                        {
                            field = f;
                            break;
                        }
                    }
                    if (field != null)
                    {
                        Object value = parseObjectByName(cursor, columnName, field.getType());
                        field.set(cusObj, value);
                    }
                }
                return cusObj;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 将查询的结果，根据名称,获取基础类型的对象
     *
     * @param cursor
     * @param columnName
     * @param type
     * @return
     */
    private Object parseObjectByName(Cursor cursor, String columnName, Class<?> type)
    {
        int columnIndex = cursor.getColumnIndex(columnName);
        Convert convert = Converts.getConvert(type);
        return convert.cursorToJavaObject(cursor, columnIndex);
    }

    /**
     * 获取函数返回List泛型的真实类型
     */
    private Class<?> getListGenericType(Method method)
    {
        Type genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType))
            throw new RuntimeException(method + " 返回类型List<>里必须写上泛型");
        ParameterizedType type = (ParameterizedType) genericReturnType;
        Type genericType = type.getActualTypeArguments()[0];
        if (!(genericType instanceof Class))
            throw new RuntimeException(method + " 返回类型List<?>里必须写上明确泛型");
        return (Class<?>) genericType;
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
