package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToBooleanConvert;

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
}
