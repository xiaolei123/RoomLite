package me.xiaolei.room_lite.runtime.adapters;

import java.lang.reflect.Type;


import me.xiaolei.room_lite.SQLiteReader;
import me.xiaolei.room_lite.runtime.sqlite.RoomLiteDatabase;

/**
 * 执行函数的返回值适配器，<br/>
 * <br/>
 * 用于支持自定义类型，比如：List&lt;T><br/>
 * <br/>
 */
public abstract class Adapter<T>
{
    private final Class<T> klass;

    public Adapter(Class<T> klass)
    {
        this.klass = klass;
    }

    public Class<T> getType()
    {
        return this.klass;
    }

    public abstract T process(Processor processor, Type generic, String[] args);
}
