package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToStringConvert;

public class CharSequenceConvert extends ToStringConvert
{
    public CharSequenceConvert()
    {
        super(CharSequence.class);
    }

    @Override
    public String convertToString(Object javaObj)
    {
        if (javaObj == null) return null;
        return javaObj.toString();
    }
}
