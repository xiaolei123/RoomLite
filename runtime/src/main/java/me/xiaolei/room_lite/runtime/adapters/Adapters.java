package me.xiaolei.room_lite.runtime.adapters;

import java.util.HashMap;
import java.util.Map;

public class Adapters
{
    private static final Map<Class<?>, Adapter<?>> adapters = new HashMap<>();
    
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
