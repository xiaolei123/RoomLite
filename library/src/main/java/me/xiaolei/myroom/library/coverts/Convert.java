package me.xiaolei.myroom.library.coverts;


import me.xiaolei.myroom.library.anno.Column;
import me.xiaolei.myroom.library.util.RoomLiteUtil;

/**
 * 转换器，从自定义类型，转换成数据库基本类型的工具
 */
public abstract class Convert
{
    private final Class<?> javaType;
    private final Column.SQLType sqlType;

    /**
     * 获取对应的Java类型
     */
    public Class<?> getJavaType()
    {
        return javaType;
    }

    /**
     * 获取对应的SQL类型
     */
    public Column.SQLType getSqlType()
    {
        return sqlType;
    }

    /**
     * @param javaType 设置对应的Java类型
     * @param sqlType  设置对应的SQL类型
     */
    public Convert(Class<?> javaType, Column.SQLType sqlType)
    {
        this.javaType = javaType;
        this.sqlType = sqlType;
    }

    /**
     * 判断传入的Java类型，是否是同一个类型，这里需要特殊处理父类，和接口类型
     *
     * @param type
     * @return
     */
    public boolean isSameType(Class<?> type)
    {
        Class<?> javaType = this.getJavaType();
        return RoomLiteUtil.isSameTypeOf(type, javaType);
    }

    public Byte convertToByte(Object javaObj)
    {
        return null;
    }

    public Long convertToLong(Object javaObj)
    {
        return null;
    }

    public Float convertToFloat(Object javaObj)
    {
        return null;
    }

    public Double convertToDouble(Object javaObj)
    {
        return null;
    }

    public Short convertToShort(Object javaObj)
    {
        return null;
    }

    public byte[] convertToByteArray(Object javaObj)
    {
        return null;
    }

    public String convertToString(Object javaObj)
    {
        return null;
    }

    public Boolean convertToBoolean(Object javaObj)
    {
        return null;
    }

    public Integer convertToInteger(Object javaObj)
    {
        return null;
    }
}
