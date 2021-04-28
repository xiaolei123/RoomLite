package me.xiaolei.room_lite.runtime.util;

import android.os.Looper;

public class RoomLiteUtil
{
    /**
     * 根据类型，获取默认值
     *
     * @param klass
     */
    public static Object defaultValue(Class<?> klass)
    {
        if (klass == int.class)
        {
            return 0;
        } else if (klass == boolean.class)
        {
            return false;
        } else if (klass == byte.class)
        {
            return (byte) 0;
        } else if (klass == char.class)
        {
            return (char) 0;
        } else if (klass == float.class)
        {
            return 0f;
        } else if (klass == double.class)
        {
            return 0d;
        } else if (klass == long.class)
        {
            return 0L;
        } else if (klass == short.class)
        {
            return (short) 0;
        } else
        {
            return null;
        }
    }

    /**
     * 检查当前线程是否运行在主线程
     */
    public static boolean checkIsMainThread()
    {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }
}
