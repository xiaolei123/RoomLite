package me.xiaolei.roomlite.room_lite;

import java.util.Date;

import me.xiaolei.room_lite.runtime.coverts.base.ToLongConvert;

public class DateConvert extends ToLongConvert<Date>
{
    public DateConvert()
    {
        super(Date.class);
    }

    @Override
    public Long convertToLong(Date javaObj)
    {
        Date date = (Date) javaObj;
        if (javaObj == null) 
            return null;
        return date.getTime();
    }

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Date cursorToJavaObject(long value)
    {
        return new Date(value);
    }
}
