package me.xiaolei.room_lite.runtime.coverts;

import java.util.HashMap;
import java.util.Map;

import me.xiaolei.room_lite.annotations.Column;
import me.xiaolei.room_lite.runtime.coverts.base.ToBooleanConvert;
import me.xiaolei.room_lite.runtime.coverts.base.ToByteArrayConvert;
import me.xiaolei.room_lite.runtime.coverts.base.ToByteConvert;
import me.xiaolei.room_lite.runtime.coverts.base.ToDoubleConvert;
import me.xiaolei.room_lite.runtime.coverts.base.ToFloatConvert;
import me.xiaolei.room_lite.runtime.coverts.base.ToIntegerConvert;
import me.xiaolei.room_lite.runtime.coverts.base.ToLongConvert;
import me.xiaolei.room_lite.runtime.coverts.base.ToShortConvert;
import me.xiaolei.room_lite.runtime.coverts.base.ToStringConvert;
import me.xiaolei.room_lite.runtime.coverts.impls.*;

public class Converts
{
    private static final Map<Class<?>, Convert> converts = new HashMap<>();

    static
    {
        addConvert(new BooleanBoxConvert());
        addConvert(new BooleanConvert());
        addConvert(new ByteArrayConvert());
        addConvert(new ByteBoxArrayConvert());
        addConvert(new ByteBoxConvert());
        addConvert(new ByteConvert());
        addConvert(new CharBoxConvert());
        addConvert(new CharConvert());
        addConvert(new StringConvert());
        addConvert(new DoubleBoxConvert());
        addConvert(new DoubleConvert());
        addConvert(new FloatBoxConvert());
        addConvert(new FloatConvert());
        addConvert(new IntConvert());
        addConvert(new IntegerConvert());
        addConvert(new LongBoxConvert());
        addConvert(new LongConvert());
        addConvert(new ShortBoxConvert());
        addConvert(new ShortConvert());
    }

    /**
     * 检查传入的类型，是不是支持的类型
     */
    public static boolean hasConvert(Class<?> javaType)
    {
        Convert convert = getConvert(javaType);
        return convert != null;
    }


    /**
     * 根据Java的类型，寻找出对应的转换器
     *
     * @param javaType
     */
    public static Convert getConvert(Class<?> javaType)
    {
        return converts.get(javaType);
    }

    /**
     * 根据Java的类型，寻找出对应的转换器上声明的数据库类型
     *
     * @param javaType
     */
    public static Column.SQLType convertSqlType(Class<?> javaType)
    {
        Convert convert = getConvert(javaType);
        if (convert != null)
        {
            return convert.getSqlType();
        }
        throw new RuntimeException(javaType.getCanonicalName() + "所对应的数据库类型转换器未定义。");
    }

    /**
     * 添加自定义类型转换器
     */
    public static void addConvert(Convert convert)
    {
        try
        {
            if (!isInnerConvert(convert))
                throw new RuntimeException(convert.getClass().getCanonicalName() + " 必须继承：\n" +
                        "ToByteConvert\n" +
                        "ToLongConvert\n" +
                        "ToFloatConvert\n" +
                        "ToDoubleConvert\n" +
                        "ToShortConvert\n" +
                        "ToByteArrayConvert\n" +
                        "ToStringConvert\n" +
                        "ToBooleanConvert\n" +
                        "ToIntegerConvert\n" +
                        "其中的一个。");
            converts.put(convert.getJavaType(), convert);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断类型是否是继承了所能支持的基本的内置类型转换器
     */
    private static boolean isInnerConvert(Convert convert)
    {
        if (convert instanceof ToByteConvert)
            return true;
        if (convert instanceof ToLongConvert)
            return true;
        if (convert instanceof ToFloatConvert)
            return true;
        if (convert instanceof ToDoubleConvert)
            return true;
        if (convert instanceof ToShortConvert)
            return true;
        if (convert instanceof ToByteArrayConvert)
            return true;
        if (convert instanceof ToStringConvert)
            return true;
        if (convert instanceof ToBooleanConvert)
            return true;
        if (convert instanceof ToIntegerConvert)
            return true;
        return false;
    }
}
