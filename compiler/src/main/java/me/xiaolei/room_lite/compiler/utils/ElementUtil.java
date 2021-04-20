package me.xiaolei.room_lite.compiler.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import me.xiaolei.room_lite.annotations.Column;
import me.xiaolei.room_lite.annotations.Entity;
import me.xiaolei.room_lite.annotations.Ignore;
import me.xiaolei.room_lite.annotations.PrimaryKey;

public class ElementUtil
{
    /**
     * 解析某个字段的属性，以及类型，获取到对应的字段名称
     */
    public static String getColumnName(VariableElement element)
    {
        Column column = element.getAnnotation(Column.class);
        String columnName;
        if (column == null || column.name().isEmpty())
        {
            columnName = element.getSimpleName().toString();
        } else
        {
            columnName = column.name();
        }
        return columnName;
    }

    /**
     * 获取某个类的所有的成员变量的，数据库支持字段
     */
    public static List<VariableElement> getFields(Element element)
    {
        TypeElement typeElement = ((TypeElement) element);
        List<? extends Element> elements = typeElement.getEnclosedElements();
        List<VariableElement> fields = new ArrayList<>();
        for (Element element1 : elements)
        {
            if (element1 instanceof VariableElement)
            {
                VariableElement field = (VariableElement) element1;
                Set<Modifier> modifiers = field.getModifiers();
                if (!modifiers.contains(Modifier.PUBLIC))
                {
                    continue;
                }
                if (field.getAnnotation(Ignore.class) != null)
                    continue;
                fields.add(field);
            }
        }
        return fields;
    }


    /**
     * 获取 @Entity 的表名称<br/>
     * 会优先去缓存里取名字，如果缓存里没有，则临时反射获取。<br/>
     * <br/>
     *
     * @param element
     * @return
     */
    public static String getTableName(Element element)
    {
        String klassName = element.getSimpleName().toString();
        Entity entity = element.getAnnotation(Entity.class);
        String tableName = entity.name();
        if (tableName.isEmpty())
        {
            tableName = klassName;
        }
        return tableName;
    }

    /**
     * 检查是否含有主键
     */
    public static boolean hasPrimaryKey(List<VariableElement> fields)
    {
        for (VariableElement field : fields)
        {
            if (isPrimaryKey(field))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断字段是否是主键
     */
    public static boolean isPrimaryKey(VariableElement element)
    {
        return element.getAnnotation(PrimaryKey.class) != null;
    }


    /**
     * 获取其他修饰符
     */
    public static String getColumnTag(VariableElement field)
    {
        StringBuilder builder = new StringBuilder();
        // ---------------------@Column-----------------------------
        Column column = field.getAnnotation(Column.class);
        if (column != null)
        {
            boolean notNull = column.notNull();
            boolean unique = column.unique();
            String defaultValue = column.defaultValue();
            // 不为空
            if (notNull)
            {
                builder.append(" NOT NULL ");
            }
            // 唯一性
            if (unique)
            {
                builder.append(" UNIQUE ");
            }
            // 默认值
            if (!defaultValue.isEmpty())
            {
                builder.append(" DEFAULT ");
                builder.append(defaultValue);
                builder.append(" ");
            }
        }
        // ---------------------@PrimaryKey-----------------------------
        PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
        if (primaryKey != null)
        {
            builder.append(" PRIMARY KEY");
            if (primaryKey.autoGenerate())
            {
                if (column != null && column.type() == Column.SQLType.INTEGER)
                {
                    builder.append(" AUTOINCREMENT");
                }
            }
        }
        // --------------------------------------------------
        return builder.toString();
    }
}
