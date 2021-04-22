package me.xiaolei.room_lite.compiler.utils;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import me.xiaolei.room_lite.annotations.Entity;

public class TypeUtil
{
    /**
     * 是个单个表
     */
    public static boolean isEntity(TypeMirror paramType)
    {
        if (paramType.getKind().isPrimitive())
        {
            return false;
        } else
            return paramType instanceof DeclaredType && ((DeclaredType) paramType).asElement().getAnnotation(Entity.class) != null;
    }

    /**
     * 是表数组
     */
    public static boolean isArray(TypeMirror paramType)
    {
        if (paramType.getKind().isPrimitive())
            return false;
        return paramType instanceof ArrayType;
    }

    /**
     * 是表数组
     */
    public static boolean isEntityArray(TypeMirror paramType)
    {
        if (paramType.getKind().isPrimitive())
            return false;
        if (paramType instanceof ArrayType)
        {
            TypeMirror type = ((ArrayType) paramType).getComponentType();
            return isEntity(type);
        } else
        {
            return false;
        }
    }

    /**
     * 是一个列表
     */
    public static boolean isList(TypeMirror paramType)
    {
        if (paramType.getKind().isPrimitive())
            return false;
        if (paramType instanceof DeclaredType)
        {
            Element element = ((DeclaredType) paramType).asElement();
            return List.class.getCanonicalName().equals(element.toString());
        }
        return false;
    }

    /**
     * 是表List集合
     */
    public static boolean isEntityList(TypeMirror paramType)
    {
        if (paramType.getKind().isPrimitive())
            return false;
        if (paramType instanceof DeclaredType)
        {
            Element element = ((DeclaredType) paramType).asElement();
            if (List.class.getCanonicalName().equals(element.toString())) // 如果是List
            {
                List<? extends TypeMirror> typeArgs = ((DeclaredType) paramType).getTypeArguments();
                if (typeArgs.isEmpty())
                    return false;
                // 获取范型类型
                TypeMirror genericType = typeArgs.get(0);
                return isEntity(genericType);
            } else
            {
                return false;
            }
        } else
        {
            return false;
        }
    }

    /**
     * 是基础类型的包装类
     */
    public static boolean isPrimitiveBox(TypeMirror typeMirror)
    {
        String typeString = typeMirror.toString();
        if (typeString.equals(Boolean.class.getCanonicalName()))
        {
            return true;
        } else if (typeString.equals(Byte.class.getCanonicalName()))
        {
            return true;
        } else if (typeString.equals(Short.class.getCanonicalName()))
        {
            return true;
        } else if (typeString.equals(Integer.class.getCanonicalName()))
        {
            return true;
        } else if (typeString.equals(Long.class.getCanonicalName()))
        {
            return true;
        } else if (typeString.equals(Character.class.getCanonicalName()))
        {
            return true;
        } else if (typeString.equals(Float.class.getCanonicalName()))
        {
            return true;
        } else if (typeString.equals(Double.class.getCanonicalName()))
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 是基础类型，或者是它的包装类
     */
    public static boolean isPrimitiveOrBox(TypeMirror typeMirror)
    {
        return typeMirror.getKind().isPrimitive() || isPrimitiveBox(typeMirror);
    }

    /**
     * 是字符串类型
     */
    public static boolean isString(TypeMirror typeMirror)
    {
        return typeMirror.toString().equals(String.class.getCanonicalName());
    }
}
