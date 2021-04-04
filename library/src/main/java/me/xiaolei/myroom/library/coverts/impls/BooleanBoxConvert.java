package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToBooleanConvert;

public class BooleanBoxConvert extends ToBooleanConvert<Boolean>
{
    public BooleanBoxConvert()
    {
        super(Boolean.class);
    }

    @Override
    public Boolean convertToBoolean(Boolean javaObj)
    {
        return javaObj;
    }

    @Override
    public Boolean cursorToJavaObject(boolean value)
    {
        return value;
    }
}
