package me.xiaolei.room_lite.runtime.dao_proxy.proxy;


import android.content.ContentValues;

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

/**
 * 新增的代理类
 */
public class InsertProxy extends DaoProxy
{
    private final List<Class<?>> entities = new CopyOnWriteArrayList<>(liteDatabase.getEntities());
    
    public InsertProxy(RoomLiteDatabase liteDatabase, LiteDataBase database)
    {
        super(liteDatabase, database);
    }

    public Object invoke(Method method, Object[] args)
    {
        // 改变行数
        int changeCount = 0;
        // 检查返回类型，插入只允许支持 void 或者 int
        Class<?> returnType = method.getReturnType();
        if (returnType != int.class && returnType != void.class)
            throw new RuntimeException(method + " 插入数据返回类型仅支持 void 或者 int");
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
            // 获取插入组的类型
            Class<?> klass = entry.getKey();
            // 获取要插入的对象合集
            List<Object> insertObjs = entry.getValue();
            // 把对象转换成ContentValues
            List<ContentValues> contentValues = new LinkedList<>();
            for (Object obj : insertObjs)
            {
                contentValues.add(RoomLiteUtil.convertContentValue(klass, obj));
            }
            // 获取类当前的表名
            String tableName = RoomLiteUtil.getTableName(klass);

            AtomicInteger count = new AtomicInteger();
            database.doTransaction((transaction) ->
            {
                for (ContentValues contentValue : contentValues)
                {
                    transaction.insert(tableName, null, contentValue);
                    count.incrementAndGet();
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
