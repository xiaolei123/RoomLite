package me.xiaolei.room_lite.runtime.coverts.impls;

import me.xiaolei.room_lite.runtime.coverts.base.ToBooleanConvert;

public class BooleanConvert extends ToBooleanConvert
{
    public BooleanConvert()
    {
        super(boolean.class);
    }

    @Override
    public Boolean convertToBoolean(Object javaObj)
    {
        return (boolean) javaObj;
    }

    @Override
    public Object cursorToJavaObject(boolean value)
    {
        return value;
    }
}
