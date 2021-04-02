package me.xiaolei.myroom.library.dao_proxy.handler;

import android.os.Looper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.xiaolei.myroom.library.anno.dao.Delete;
import me.xiaolei.myroom.library.anno.dao.Insert;
import me.xiaolei.myroom.library.anno.dao.Query;
import me.xiaolei.myroom.library.anno.dao.Update;
import me.xiaolei.myroom.library.dao_proxy.DaoProxy;
import me.xiaolei.myroom.library.dao_proxy.proxy.DeleteProxy;
import me.xiaolei.myroom.library.dao_proxy.proxy.InsertProxy;
import me.xiaolei.myroom.library.dao_proxy.proxy.QueryProxy;
import me.xiaolei.myroom.library.dao_proxy.proxy.UpdateProxy;
import me.xiaolei.myroom.library.sqlite.BaseDatabase;
import me.xiaolei.myroom.library.sqlite.RoomLiteDatabase;

/**
 * 代理总分发
 */
public class DaoProxyHandler extends BaseProxyHandler
{
    private static final Map<RoomLiteDatabase, DaoProxyHandler> handlerCache = new ConcurrentHashMap<>();
    // 增
    private final DaoProxy insertProxy;
    // 删
    private final DaoProxy deleteProxy;
    // 改
    private final DaoProxy updateProxy;
    // 查
    private final DaoProxy queryProxy;
    // 持有数据库的引用
    private final RoomLiteDatabase liteDatabase;


    public static InvocationHandler getHandler(RoomLiteDatabase liteDatabase, BaseDatabase database)
    {
        DaoProxyHandler handler = handlerCache.get(liteDatabase);
        if (handler == null)
        {
            synchronized (DaoProxyHandler.class)
            {
                handler = new DaoProxyHandler(liteDatabase, database);
                handlerCache.put(liteDatabase, handler);
            }
        }
        return handler;
    }

    private DaoProxyHandler(RoomLiteDatabase liteDatabase, BaseDatabase database)
    {
        this.liteDatabase = liteDatabase;
        insertProxy = new InsertProxy(liteDatabase, database);
        deleteProxy = new DeleteProxy(liteDatabase, database);
        updateProxy = new UpdateProxy(liteDatabase, database);
        queryProxy = new QueryProxy(liteDatabase, database);
    }

    @Override
    public Object invoke(Method method, Object[] args)
    {
        // 这里根据当前执行函数上的注解，分发选择不同的代理类去执行
        if (method.isAnnotationPresent(Insert.class))
        {
            if (!this.liteDatabase.allowRunOnUIThread())
                checkMainThread();
            return insertProxy.invoke(method, args);
        } else if (method.isAnnotationPresent(Delete.class))
        {
            if (!this.liteDatabase.allowRunOnUIThread())
                checkMainThread();
            return deleteProxy.invoke(method, args);
        } else if (method.isAnnotationPresent(Update.class))
        {
            if (!this.liteDatabase.allowRunOnUIThread())
                checkMainThread();
            return updateProxy.invoke(method, args);
        } else if (method.isAnnotationPresent(Query.class))
        {
            if (!this.liteDatabase.allowRunOnUIThread())
                checkMainThread();
            return queryProxy.invoke(method, args);
        }
        throw new RuntimeException(method.getDeclaringClass().getCanonicalName() + "." + method.getName() + " 必须使用@Insert/@Delete/@Update/@Query 配合修饰使用");
    }

    /**
     * 检查是不是运行在主线程，如果运行在主线程，则爆出异常
     */
    private void checkMainThread()
    {
        Looper mainLooper = Looper.getMainLooper();
        if (mainLooper.getThread() == Thread.currentThread())
        {
            throw new RuntimeException("不可以在主线程中执行，如果想在主线程执行，请重写:" + RoomLiteDatabase.class
                    + ":\npublic boolean allowRunOnUIThread()\n" +
                    "{\n" +
                    "    return true;\n" +
                    "}");
        }
    }
}
