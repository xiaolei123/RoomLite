package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToBooleanConvert;

public class BooleanBoxConvert extends ToBooleanConvert
{
    public BooleanBoxConvert()
    {
        super(Boolean.class);
    }

    @Override
    public Boolean convertToBoolean(Object javaObj)
    {
        return (Boolean) javaObj;
    }
}
