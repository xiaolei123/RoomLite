package me.xiaolei.myroom.library.coverts;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import me.xiaolei.myroom.library.anno.Column;
import me.xiaolei.myroom.library.coverts.base.ToBooleanConvert;
import me.xiaolei.myroom.library.coverts.base.ToByteArrayConvert;
import me.xiaolei.myroom.library.coverts.base.ToByteConvert;
import me.xiaolei.myroom.library.coverts.base.ToDoubleConvert;
import me.xiaolei.myroom.library.coverts.base.ToFloatConvert;
import me.xiaolei.myroom.library.coverts.base.ToIntegerConvert;
import me.xiaolei.myroom.library.coverts.base.ToLongConvert;
import me.xiaolei.myroom.library.coverts.base.ToShortConvert;
import me.xiaolei.myroom.library.coverts.base.ToStringConvert;
import me.xiaolei.myroom.library.coverts.impls.*;

public class Converts
{
    private static final List<Convert> converts = new CopyOnWriteArrayList<>();

    static
    {
        converts.add(new BooleanBoxConvert());
        converts.add(new BooleanConvert());
        converts.add(new ByteArrayConvert());
        converts.add(new ByteBoxArrayConvert());
        converts.add(new ByteBoxConvert());
        converts.add(new ByteConvert());
        converts.add(new CharBoxConvert());
        converts.add(new CharConvert());
        converts.add(new StringConvert());
        converts.add(new DoubleBoxConvert());
        converts.add(new DoubleConvert());
        converts.add(new FloatBoxConvert());
        converts.add(new FloatConvert());
        converts.add(new IntConvert());
        converts.add(new IntegerConvert());
        converts.add(new LongBoxConvert());
        converts.add(new LongConvert());
        converts.add(new ShortBoxConvert());
        converts.add(new ShortConvert());
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
        for (Convert convert : converts)
        {
            if (convert.getJavaType() == javaType)
                return convert;
        }
        return null;
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
    public static void addConvert(Class<? extends Convert> convertKlass)
    {
        try
        {
            if (!isInnerConvert(convertKlass))
                throw new RuntimeException(convertKlass.getCanonicalName() + " 必须继承：\n" +
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
            Convert convert = convertKlass.newInstance();
            converts.add(convert);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断类型是否是继承了所能支持的基本的内置类型转换器
     */
    private static boolean isInnerConvert(Class<?> klass)
    {
        if (klass == ToByteConvert.class)
            return true;
        if (klass == ToLongConvert.class)
            return true;
        if (klass == ToFloatConvert.class)
            return true;
        if (klass == ToDoubleConvert.class)
            return true;
        if (klass == ToShortConvert.class)
            return true;
        if (klass == ToByteArrayConvert.class)
            return true;
        if (klass == ToStringConvert.class)
            return true;
        if (klass == ToBooleanConvert.class)
            return true;
        if (klass == ToIntegerConvert.class)
            return true;
        Class<?> superKlass = klass.getSuperclass();
        if (superKlass == null || superKlass == Object.class)
            return false;
        return isInnerConvert(superKlass);
    }
}
