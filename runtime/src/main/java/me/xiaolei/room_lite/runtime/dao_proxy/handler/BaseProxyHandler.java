package me.xiaolei.room_lite.runtime.dao_proxy.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 基础代理类
 */
public abstract class BaseProxyHandler implements InvocationHandler
{
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        if (method.getName().equals("toString") && args == null && method.getReturnType() == String.class)
        {
            return this.proxyToString(proxy);
        }
        return invoke(method, args);
    }

    public abstract Object invoke(Method method, Object[] args);

    private String proxyToString(Object proxy)
    {
        return proxy.getClass().getSimpleName() + "@Dao";
    }
}
