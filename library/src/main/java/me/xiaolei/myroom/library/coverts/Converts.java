package me.xiaolei.myroom.library.coverts;

import java.util.concurrent.LinkedBlockingQueue;

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
    private static final LinkedBlockingQueue<Convert> converts = new LinkedBlockingQueue<>();

    static
    {
        converts.offer(new BooleanBoxConvert());
        converts.offer(new BooleanConvert());
        converts.offer(new ByteArrayConvert());
        converts.offer(new ByteBoxArrayConvert());
        converts.offer(new ByteBoxConvert());
        converts.offer(new ByteConvert());
        converts.offer(new CharBoxConvert());
        converts.offer(new CharConvert());
        converts.offer(new StringConvert());
        converts.offer(new DoubleBoxConvert());
        converts.offer(new DoubleConvert());
        converts.offer(new FloatBoxConvert());
        converts.offer(new FloatConvert());
        converts.offer(new IntConvert());
        converts.offer(new IntegerConvert());
        converts.offer(new LongBoxConvert());
        converts.offer(new LongConvert());
        converts.offer(new ShortBoxConvert());
        converts.offer(new ShortConvert());
    }

    /**
     * 检查传入的类型，是不是支持的类型
     */
    public static boolean hasConvert(Class<?> javaType)
    {
        for (Convert convert : converts)
        {
            if (convert.isSameType(javaType))
                return true;
        }
        return false;
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
            if (convert.isSameType(javaType))
                return convert;
        }
        throw new RuntimeException(javaType.getCanonicalName() + "所对应的数据库类型转换器未定义。");
    }

    /**
     * 根据Java的类型，寻找出对应的转换器上声明的数据库类型
     *
     * @param javaType
     */
    public static Column.SQLType convertSqlType(Class<?> javaType)
    {
        for (Convert convert : converts)
        {
            if (convert.isSameType(javaType))
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
            converts.offer(convert);
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
