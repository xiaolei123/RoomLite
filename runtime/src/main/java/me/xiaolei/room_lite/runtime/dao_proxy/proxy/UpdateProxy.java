package me.xiaolei.room_lite.runtime.dao_proxy.proxy;

import android.content.ContentValues;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import me.xiaolei.room_lite.runtime.dao_proxy.DaoProxy;
import me.xiaolei.room_lite.runtime.sqlite.LiteDataBase;
import me.xiaolei.room_lite.runtime.sqlite.RoomLiteDatabase;
import me.xiaolei.room_lite.runtime.util.RoomLiteUtil;

public class UpdateProxy extends DaoProxy
{
    private final List<Class<?>> entities = new CopyOnWriteArrayList<>(liteDatabase.getEntities());

    public UpdateProxy(RoomLiteDatabase liteDatabase, LiteDataBase database)
    {
        super(liteDatabase, database);
    }

    public Object invoke(Method method, Object[] args)
    {
        // 改变行数
        int changeCount = 0;
        // 检查返回类型，更新只允许支持 void 或者 int
        Class<?> returnType = method.getReturnType();
        if (returnType != int.class && returnType != void.class)
            throw new RuntimeException(method + " 风险数据返回类型仅支持 void 或者 int");
        // 没有参数，则直接返回 0
        if (args == null || args.length == 0)
        {
            return changeCount;
        }
        // 检查传过来的参数是不是在这个数据库中
        // 对传过来的数据进行分类
        Map<Class<?>, List<Object>> assort = new HashMap<>();
        for (Object arg : args)
        {
            Class<?> objklass = arg.getClass();
            if (objklass.isArray()) // 判断数组
            {
                this.array((Object[]) arg, entities, assort);
            } else if (arg instanceof List) // 判断list
            {
                this.list((List) arg, entities, assort);
            } else // 单个对象
            {
                this.single(arg, entities, assort);
            }
        }
        // 对分类好的数据，进行解析
        for (Map.Entry<Class<?>, List<Object>> entry : assort.entrySet())
        {
            // 获取更新组的类型
            Class<?> klass = entry.getKey();
            // 获取要更新的对象合集
            List<Object> updateObjs = entry.getValue();
            // 获取类当前的表名
            String tableName = liteDatabase.getEntityHelper(klass).getTableName();
            // 获取所有的主键字段
            List<Field> keyFields = RoomLiteUtil.getPrimaryKeyField(klass);
            // 生成更新条件
            StringBuilder whereClause = new StringBuilder();
            for (int i = 0; i < keyFields.size(); i++)
            {
                Field keyField = keyFields.get(i);
                // 获取表字段名
                String columnName = RoomLiteUtil.getColumnName(keyField);
                whereClause.append(columnName).append("=?");
                if (i < keyFields.size() - 1)
                {
                    whereClause.append(" AND");
                }
            }
            // 获取所有对象的whereClause的值
            List<String[]> whereArgss = new LinkedList<>();
            List<ContentValues> contentValues = new LinkedList<>();
            for (Object updateObj : updateObjs)
            {
                String[] values = new String[keyFields.size()];
                for (int i = 0; i < keyFields.size(); i++)
                {
                    Field keyField = keyFields.get(i);
                    // 获取某个主键字段的值，并生成字符串
                    values[i] = String.valueOf(RoomLiteUtil.getFieldValue(updateObj, keyField));
                }
                whereArgss.add(values);
                // 获取这个对象转换成的ContentValues
                contentValues.add(RoomLiteUtil.convertContentValue(klass, updateObj));
            }

            AtomicInteger count = new AtomicInteger(0);
            database.doTransaction(transaction ->
            {
                for (int i = 0; i < whereArgss.size(); i++)
                {
                    String[] whereArgs = whereArgss.get(i);
                    ContentValues values = contentValues.get(i);
                    int changeRow = transaction.update(tableName, values, whereClause.toString(), whereArgs);
                    count.addAndGet(changeRow);
                }
            });
            changeCount += count.get();
        }
        return changeCount;
    }


    /**
     * 处理单个对象
     *
     * @param arg
     * @param entities
     * @param assort
     */
    private void single(Object arg, List<Class<?>> entities, Map<Class<?>, List<Object>> assort)
    {
        Class<?> objklass = arg.getClass();

        if (!entities.contains(objklass))
        {
            throw new RuntimeException(objklass + " 对应的entity 不属于数据库:" + liteDatabase.getDatabaseName());
        }
        List<Object> objs = assort.get(objklass);
        if (objs == null)
        {
            objs = new LinkedList<>();
            assort.put(objklass, objs);
        }
        objs.add(arg);
    }

    /**
     * 处理数组
     *
     * @param args
     * @param entities
     * @param assort
     */
    private void array(Object[] args, List<Class<?>> entities, Map<Class<?>, List<Object>> assort)
    {
        for (Object arg : args)
        {
            this.single(arg, entities, assort);
        }
    }

    /**
     * 处理列表
     *
     * @param args
     * @param entities
     * @param assort
     */
    private void list(List<Object> args, List<Class<?>> entities, Map<Class<?>, List<Object>> assort)
    {
        for (Object arg : args)
        {
            this.single(arg, entities, assort);
        }
    }
}
