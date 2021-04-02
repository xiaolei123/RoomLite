package me.xiaolei.myroom.library.util;

import android.content.ContentValues;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.xiaolei.myroom.library.anno.Column;
import me.xiaolei.myroom.library.anno.Entity;
import me.xiaolei.myroom.library.anno.Ignore;
import me.xiaolei.myroom.library.anno.PrimaryKey;
import me.xiaolei.myroom.library.coverts.Convert;
import me.xiaolei.myroom.library.coverts.Converts;
import me.xiaolei.myroom.library.coverts.base.ToBooleanConvert;
import me.xiaolei.myroom.library.coverts.base.ToByteArrayConvert;
import me.xiaolei.myroom.library.coverts.base.ToByteConvert;
import me.xiaolei.myroom.library.coverts.base.ToDoubleConvert;
import me.xiaolei.myroom.library.coverts.base.ToFloatConvert;
import me.xiaolei.myroom.library.coverts.base.ToIntegerConvert;
import me.xiaolei.myroom.library.coverts.base.ToLongConvert;
import me.xiaolei.myroom.library.coverts.base.ToShortConvert;
import me.xiaolei.myroom.library.coverts.base.ToStringConvert;

public class RoomLiteUtil
{
    /**
     * 建表工具
     */
    public static final CreateSqlUtil Create = new CreateSqlUtil();

    /**
     * 获取 @Entity 的表名称
     *
     * @param klass
     * @return
     */
    public static String getTableName(Class<?> klass)
    {
        Entity entity = klass.getAnnotation(Entity.class);
        if (entity == null)
        {
            return null;
        }
        String tableName = entity.name();
        return (tableName.isEmpty()) ? klass.getSimpleName() : tableName;
    }

    /**
     * 解析字段的，对应数据库的字段名称
     */
    public static String getColumnName(Field field)
    {
        Column column = field.getAnnotation(Column.class);
        if (column == null || column.name().isEmpty())
        {
            return field.getName();
        } else
        {
            return column.name();
        }
    }

    private final static Map<Class<?>, List<Field>> fieldCache = new ConcurrentHashMap<>();

    /**
     * 获取一个entity class的所有的字段,并且自动忽略标记忽略的，或者不可访问的
     *
     * @param klass
     * @return
     */
    public static List<Field> getFields(Class<?> klass)
    {
        List<Field> fields = fieldCache.get(klass);
        if (fields == null)
        {
            fields = new LinkedList<>();
            fieldCache.put(klass, fields);
            Field[] dfields = klass.getDeclaredFields();
            for (Field field : dfields)
            {
                if (field.isAnnotationPresent(Ignore.class))
                    continue;
                if (!Modifier.isPublic(field.getModifiers()))
                    continue;
                fields.add(field);
            }
        }
        return fields;
    }

    /**
     * 获取一个表中的所有主键的字段
     *
     * @param klass
     */
    public static List<Field> getPrimaryKeyField(Class<?> klass)
    {
        List<Field> primaryField = new LinkedList<>();
        List<Field> allField = getFields(klass);
        for (Field field : allField)
        {
            if (RoomLiteUtil.isPrimaryKey(field))
            {
                primaryField.add(field);
            }
        }
        return primaryField;
    }

    /**
     * 获取对应的SQL语句的字段类型
     */
    public static Column.SQLType getColumnSQLType(Field field)
    {
        Column column = field.getAnnotation(Column.class);
        if (column == null || Column.SQLType.UNDEFINED.equals(column.type()))
        {
            return Converts.convertSqlType(field.getType());
        } else
        {
            return column.type();
        }
    }

    /**
     * 判断字段是否是主键，并且自动递增
     */
    public static boolean isPrimaryKeyAndAutoGenerate(Field field)
    {
        PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
        return (isPrimaryKey(field) && primaryKey.autoGenerate());
    }

    /**
     * 判断字段是否是主键
     *
     * @param field
     */
    public static boolean isPrimaryKey(Field field)
    {
        return field.isAnnotationPresent(PrimaryKey.class);
    }

    /**
     * 通过反射，获取一个某个对象的某个字段的值
     *
     * @param obj
     * @param field
     * @return
     */
    public static Object getFieldValue(Object obj, Field field)
    {
        try
        {
            return field.get(obj) + "";
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据类型，将一个对象，转换成原生API插入数据时，需要用到的 ContentValues
     * <p>
     * 自动忽略自增长的字段
     *
     * @param klass
     * @param obj
     */
    public static ContentValues convertContentValue(Class<?> klass, Object obj)
    {
        ContentValues values = new ContentValues();
        // 获取所有的字段
        List<Field> fields = RoomLiteUtil.getFields(klass);
        for (Field field : fields)
        {
            // 获取对应字段值
            Object value = RoomLiteUtil.getFieldValue(obj, field);
            // 获取对应字段名称
            String columnName = RoomLiteUtil.getColumnName(field);
            // 获取对应字段的类型
            Class<?> javaType = field.getType();
            // 根据字段类型，获取转换器
            Convert convert = Converts.getConvert(javaType);
            // 判断字段的类型是不是数字
            if (convert.getSqlType() == Column.SQLType.INTEGER)
            {
                // 如果是数字，并且标记为主键，并且是自增长的，那么就不添加此字段
                if (RoomLiteUtil.isPrimaryKeyAndAutoGenerate(field))
                    continue;
            }
            // 用转换器的类型，去跟基本支持类型去比对
            if (convert instanceof ToByteConvert)
            {
                values.put(columnName, convert.convertToByte(value));
            } else if (convert instanceof ToLongConvert)
            {
                values.put(columnName, convert.convertToLong(value));
            } else if (convert instanceof ToFloatConvert)
            {
                values.put(columnName, convert.convertToFloat(value));
            } else if (convert instanceof ToDoubleConvert)
            {
                values.put(columnName, convert.convertToDouble(value));
            } else if (convert instanceof ToShortConvert)
            {
                values.put(columnName, convert.convertToShort(value));
            } else if (convert instanceof ToByteArrayConvert)
            {
                values.put(columnName, convert.convertToByteArray(value));
            } else if (convert instanceof ToStringConvert)
            {
                values.put(columnName, convert.convertToString(value));
            } else if (convert instanceof ToBooleanConvert)
            {
                values.put(columnName, convert.convertToBoolean(value));
            } else if (convert instanceof ToIntegerConvert)
            {
                values.put(columnName, convert.convertToInteger(value));
            }
        }
        return values;
    }

    /**
     * 判断第一个Type是不是第二个Type的子类，或者是实现类
     *
     * @param type
     * @param ofType
     */
    public static boolean isSameTypeOf(Class<?> type, Class<?> ofType)
    {
        if (type == ofType)
        {
            return true;
        } else
        {
            Class<?> superClass = type.getSuperclass();
            if (superClass == null)
            {
                return false;
            } else if (superClass == ofType)
            {
                return true;
            } else if (superClass == Object.class)
            {
                Class<?>[] interfaces = type.getInterfaces();
                for (Class<?> anInterface : interfaces)
                {
                    if (anInterface == ofType)
                    {
                        return true;
                    }
                }
                return false;
            } else
            {
                return isSameTypeOf(superClass, ofType);
            }
        }
    }

    /**
     * 根据类型，获取默认值
     *
     * @param klass
     */
    public static Object defaultValue(Class<?> klass)
    {
        if (klass == int.class)
        {
            return 0;
        } else if (klass == boolean.class)
        {
            return false;
        } else if (klass == byte.class)
        {
            return (byte) 0;
        } else if (klass == char.class)
        {
            return (char) 0;
        } else if (klass == float.class)
        {
            return 0f;
        } else if (klass == double.class)
        {
            return 0d;
        } else if (klass == long.class)
        {
            return 0L;
        } else if (klass == short.class)
        {
            return (short) 0;
        } else
        {
            return null;
        }
    }
}
