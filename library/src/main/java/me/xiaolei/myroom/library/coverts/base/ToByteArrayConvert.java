package me.xiaolei.myroom.library.coverts.base;


import me.xiaolei.myroom.library.anno.Column;
import me.xiaolei.myroom.library.coverts.Convert;

public abstract class ToByteArrayConvert extends Convert
{
    /**
     * @param javaType 设置对应的Java类型
     */
    public ToByteArrayConvert(Class<?> javaType)
    {
        super(javaType, Column.SQLType.BLOB);
    }

    @Override
    public abstract byte[] convertToByteArray(Object javaObj);
}
