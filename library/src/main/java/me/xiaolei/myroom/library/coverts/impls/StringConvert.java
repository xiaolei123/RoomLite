package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToStringConvert;

public class StringConvert extends ToStringConvert
{
    public StringConvert()
    {
        super(String.class);
    }

    @Override
    public String convertToString(Object javaObj)
    {
        if (javaObj == null) return null;
        return javaObj.toString();
    }
}
