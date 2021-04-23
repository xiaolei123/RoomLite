package me.xiaolei.room_lite.runtime.adapters;

import java.util.HashMap;
import java.util.Map;

import me.xiaolei.room_lite.runtime.adapters.Impls.LiveDataAdapter;

/**
 * 保存所有的适配器
 */
public class Adapters
{
    private static final Map<Class<?>, Adapter<?>> adapters = new HashMap<>();

    static
    {
        addAdapter(new LiveDataAdapter());
    }

    public static void addAdapter(Adapter<?> adapter)
    {
        Class<?> type = adapter.getType();
        adapters.put(type, adapter);
    }

    public static Adapter<?> getAdapter(Class<?> klass)
    {
        return adapters.get(klass);
    }
}
