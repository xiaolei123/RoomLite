package me.xiaolei.room_lite.runtime.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import me.xiaolei.room_lite.annotations.Column;
import me.xiaolei.room_lite.annotations.Ignore;
import me.xiaolei.room_lite.annotations.PrimaryKey;

public class RoomLiteUtil
{
    // 缓存字段名称
    private final static Map<Field, String> columnNameMapper = new ConcurrentHashMap<>();

    /**
     * 解析字段的，对应数据库的字段名称<br/>
     * <br/>
     * 会优先去缓存里获取，获取不到，则通过反射进行获取<br/>
     * <br/>
     */
    public static String getColumnName(Field field)
    {
        String columnName = columnNameMapper.get(field);
        if (columnName == null)
        {
            Column column = field.getAnnotation(Column.class);
            if (column == null || column.name().isEmpty())
            {
                columnName = field.getName();
            } else
            {
                columnName = column.name();
            }
            columnNameMapper.put(field, columnName);
        }
        return columnName;
    }

    private final static Map<Class<?>, List<Field>> fieldCacheMapper = new ConcurrentHashMap<>();

    /**
     * 获取一个entity class的所有的字段,并且自动忽略标记忽略的，或者不可访问的<br/>
     * <br/>
     * 会优先去缓存里获取，如果获取不到，则进行反射获取<br/>
     * <br/>
     *
     * @param klass
     * @return
     */
    public static List<Field> getFields(Class<?> klass)
    {
        List<Field> fields = fieldCacheMapper.get(klass);
        if (fields == null)
        {
            fields = new CopyOnWriteArrayList<>();
            Field[] dfields = klass.getDeclaredFields();
            for (Field field : dfields)
            {
                if (field.isAnnotationPresent(Ignore.class))
                    continue;
                if (!Modifier.isPublic(field.getModifiers()))
                    continue;
                fields.add(field);
            }
            fieldCacheMapper.put(klass, fields);
        }
        return fields;
    }

    // 主键缓存
    private final static Map<Class<?>, List<Field>> primaryKeyMapper = new ConcurrentHashMap<>();

    /**
     * 获取一个表中的所有主键的字段<br/>
     * <br/>
     * 会优先去缓存里获取，如果获取不到，则进行反射获取<br/>
     * <br/>
     * @param klass
     */
    public static List<Field> getPrimaryKeyField(Class<?> klass)
    {
        List<Field> primaryField = primaryKeyMapper.get(klass);
        if (primaryField == null)
        {
            primaryField = new ArrayList<>();
            List<Field> allField = getFields(klass);
            for (Field field : allField)
            {
                if (RoomLiteUtil.isPrimaryKey(field))
                {
                    primaryField.add(field);
                }
            }
            primaryKeyMapper.put(klass, primaryField);
        }
        return primaryField;
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
            return field.get(obj);
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return null;
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
