package me.xiaolei.room_lite.compiler.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import me.xiaolei.room_lite.annotations.Column;
import me.xiaolei.room_lite.annotations.Ignore;

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
}
