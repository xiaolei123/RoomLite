package me.xiaolei.room_lite.runtime.util;

import me.xiaolei.room_lite.runtime.adapters.Adapter;
import me.xiaolei.room_lite.runtime.adapters.Adapters;
import me.xiaolei.room_lite.runtime.coverts.Convert;
import me.xiaolei.room_lite.runtime.coverts.Converts;
import me.xiaolei.room_lite.runtime.coverts.base.ToBooleanConvert;
import me.xiaolei.room_lite.runtime.coverts.base.ToByteArrayConvert;
import me.xiaolei.room_lite.runtime.coverts.base.ToByteConvert;
import me.xiaolei.room_lite.runtime.coverts.base.ToDoubleConvert;
import me.xiaolei.room_lite.runtime.coverts.base.ToFloatConvert;
import me.xiaolei.room_lite.runtime.coverts.base.ToIntegerConvert;
import me.xiaolei.room_lite.runtime.coverts.base.ToLongConvert;
import me.xiaolei.room_lite.runtime.coverts.base.ToShortConvert;
import me.xiaolei.room_lite.runtime.coverts.base.ToStringConvert;

public abstract class AutoRegister
{

    public abstract void register();

    protected void regist(Class klass)
    {
        if (isSubType(klass, Adapter.class))
        {
            try
            {
                Adapters.addAdapter((Adapter) (klass.newInstance()));
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        } else if (isSubType(klass, ToBooleanConvert.class) ||
                isSubType(klass, ToByteArrayConvert.class) ||
                isSubType(klass, ToByteConvert.class) ||
                isSubType(klass, ToDoubleConvert.class) ||
                isSubType(klass, ToFloatConvert.class) ||
                isSubType(klass, ToIntegerConvert.class) ||
                isSubType(klass, ToLongConvert.class) ||
                isSubType(klass, ToShortConvert.class) ||
                isSubType(klass, ToStringConvert.class))
        {
            try
            {
                Converts.addConvert((Convert) (klass.newInstance()));
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isSubType(Class klass, Class parentKlass)
    {
        Class[] interfaces = klass.getInterfaces();
        Class superClass = klass.getSuperclass();
        if (superClass == null || superClass == Object.class)
            return false;
        for (Class anInterface : interfaces)
        {
            if (anInterface == parentKlass)
                return true;
        }
        if (superClass == parentKlass)
            return true;
        return isSubType(superClass, parentKlass);
    }
}
