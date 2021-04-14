package me.xiaolei.room_lite.library.util;

import android.database.Cursor;

import java.lang.reflect.Field;
import java.util.List;

import me.xiaolei.room_lite.library.coverts.Convert;
import me.xiaolei.room_lite.library.coverts.Converts;

public class QueryUtil
{
    /**
     * 将查询的结果，转换成type所对应的类型对象
     */
    public static Object parseObject(Cursor cursor, Class<?> type)
    {
        String[] columnNames = cursor.getColumnNames();
        if (Converts.hasConvert(type)) // 解析基本类型
        {
            return parseObjectByName(cursor, columnNames[0], type);
        } else // 解析自定义类型
        {
            try
            {
                List<Field> fields = RoomLiteUtil.getFields(type);
                Object cusObj = type.newInstance();
                for (String columnName : columnNames)
                {
                    Field field = null;
                    for (Field f : fields)
                    {
                        if (columnName.equals(RoomLiteUtil.getColumnName(f)))
                        {
                            field = f;
                            break;
                        }
                    }
                    if (field != null)
                    {
                        Object value = parseObjectByName(cursor, columnName, field.getType());
                        field.set(cusObj, value);
                    }
                }
                return cusObj;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 将查询的结果，根据名称,获取基础类型的对象
     *
     * @param cursor
     * @param columnName
     * @param type
     * @return
     */
    public static Object parseObjectByName(Cursor cursor, String columnName, Class<?> type)
    {
        int columnIndex = cursor.getColumnIndex(columnName);
        Convert convert = Converts.getConvert(type);
        if (convert == null)
            throw new RuntimeException(type.getCanonicalName() + "所对应的数据库类型转换器未定义。");
        return convert.cursorToJavaObject(cursor, columnIndex);
    }
}
