package me.xiaolei.room_lite.runtime.util;

import java.util.ArrayList;
import java.util.Arrays;

public class LiteArrayList<T> extends java.util.ArrayList<T>
{
    public LiteArrayList()
    {
        super();
    }

    public LiteArrayList(T[] array)
    {
        super(array == null ? new ArrayList<>() : Arrays.asList(array));
    }

    public T findFirst(Compare<T> option)
    {
        for (T obj : this)
        {
            if (option.option(obj))
                return obj;
        }
        return null;
    }
    
    public boolean contains(Compare<T> option)
    {
        for (T obj : this)
        {
            if (option.option(obj))
                return true;
        }
        return false;
    }
    

    public static interface Compare<T>
    {
        public boolean option(T obj);
    }
}
