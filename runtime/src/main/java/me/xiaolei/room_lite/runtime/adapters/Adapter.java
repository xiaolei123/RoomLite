package me.xiaolei.room_lite.runtime.adapters;

import java.lang.reflect.Type;

import me.xiaolei.room_lite.runtime.sqlite.RoomLiteDatabase;

/**
 * 执行函数的返回值适配器，<br/>
 * <br/>
 * 用于支持自定义类型，比如：LiveData&lt;T> RxJava<br/>
 * <br/>
 */
public abstract class Adapter<T>
{
    private final Class<T> klass;

    public Adapter(Class<T> klass)
    {
        this.klass = klass;
    }

    /**
     * 获取转换器支持的类型
     */
    public Class<T> getType()
    {
        return this.klass;
    }

    /**
     * 转换
     *
     * @param processor 一般会使用这个参数，queryCursor获取Cursor 然后通过process转换成对应的对象
     * @param database  对应的数据库
     * @param generic   范型
     * @param sql       SQL语句
     * @param args      SQL语句的参数
     * @return 返回对应的类型的容器对象
     */
    public abstract T process(Processor processor, RoomLiteDatabase database, Type generic, String sql, String[] args);
}
