package me.xiaolei.myroom.library.adapters;

import java.lang.reflect.Type;

import me.xiaolei.myroom.library.sqlite.BaseDatabase;

/**
 * 执行函数的返回值适配器，<br/>
 * <br/>
 * 用于支持自定义类型，比如：List&lt;T><br/>
 * <br/>
 */
public abstract class ContainerAdapter<T>
{
    private final Class<T> klass;

    public ContainerAdapter(Class<T> klass)
    {
        this.klass = klass;
    }

    public Class<T> getType()
    {
        return this.klass;
    }

    public abstract T newInstance(BaseDatabase database, Type genericType, String sql, String[] args);
}
