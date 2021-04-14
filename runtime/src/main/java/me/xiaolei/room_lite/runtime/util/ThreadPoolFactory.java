package me.xiaolei.room_lite.runtime.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 给读写线程池取名字
 */
public class ThreadPoolFactory implements ThreadFactory
{
    private final AtomicInteger id = new AtomicInteger(0);
    private final String group;

    public ThreadPoolFactory(String group)
    {
        this.group = group;
    }

    @Override
    public Thread newThread(Runnable r)
    {
        Thread t = new Thread(r, group + "-" + id.getAndAdd(1));
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
