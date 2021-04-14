package me.xiaolei.room_lite.library.adapters;

import java.util.HashMap;
import java.util.Map;

import me.xiaolei.room_lite.library.adapters.impl.ListAdapter;

public class Adapters
{
    private static final Map<Class<?>, ContainerAdapter<?>> adapters = new HashMap<>();

    static
    {
        addAdapter(new ListAdapter());
    }

    public static void addAdapter(ContainerAdapter<?> adapter)
    {
        Class<?> type = adapter.getType();
        adapters.put(type, adapter);
    }

    public static ContainerAdapter<?> getAdapter(Class<?> klass)
    {
        return adapters.get(klass);
    }
}
