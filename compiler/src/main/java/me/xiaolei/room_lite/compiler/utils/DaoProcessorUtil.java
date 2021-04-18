package me.xiaolei.room_lite.compiler.utils;

import com.squareup.javapoet.MethodSpec;

import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import me.xiaolei.room_lite.annotations.Entity;
import me.xiaolei.room_lite.annotations.dao.Delete;
import me.xiaolei.room_lite.annotations.dao.Insert;
import me.xiaolei.room_lite.annotations.dao.Query;
import me.xiaolei.room_lite.annotations.dao.Update;

public class DaoProcessorUtil
{
    protected Logger logger;
    protected Elements elementUtil;
    protected Filer filer;
    protected Types typeUtil;

    public DaoProcessorUtil(Logger logger, Elements elementUtil, Filer filer, Types typeUtil)
    {
        this.logger = logger;
        this.elementUtil = elementUtil;
        this.filer = filer;
        this.typeUtil = typeUtil;
    }

    /**
     * 是个单个表
     */
    private boolean isEntity(TypeMirror paramType)
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
    private boolean isEntityArray(TypeMirror paramType)
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
     * 是表List集合
     */
    private boolean isEntityList(TypeMirror paramType)
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
     * 插入
     *
     * @param builder    函数重构
     * @param insert     插入注解
     * @param params     参数
     * @param returnType 返回参数
     */
    public void insert(MethodSpec.Builder builder, ExecutableElement method, Insert insert, List<? extends VariableElement> params, TypeMirror returnType)
    {
        builder.addStatement("throw new $T($S)", RuntimeException.class, "函数尚未实现");
        // 首先检查返回类型合不合法
        TypeKind kind = returnType.getKind();
        if (kind != TypeKind.INT && kind != TypeKind.VOID)
        {
            throw new RuntimeException(method + "@Insert 注解支持的返回类型为: int void");
        }

        for (VariableElement param : params)
        {
            TypeMirror paramType = param.asType();
            if (isEntityArray(paramType)) // Entity数组
            {
                System.out.println("EntityArray");
            } else if (isEntityList(paramType)) // Entity集合
            {
                System.out.println("EntityList");
            } else if (isEntity(paramType)) // 单个Entity
            {
                System.out.println("Entity");
            } else
            {
                throw new RuntimeException(method + ":支持的参数类型为: Entity / Entity[] / List<Entity>");
            }
        }
    }

    /**
     * 删除
     *
     * @param builder    函数重构
     * @param delete     删除注解
     * @param params     参数
     * @param returnType 返回参数
     */
    public void delete(MethodSpec.Builder builder, ExecutableElement method, Delete delete, List<? extends VariableElement> params, TypeMirror returnType)
    {
        builder.addStatement("throw new $T($S)", RuntimeException.class, "函数尚未实现");
    }

    /**
     * 更新
     *
     * @param builder    函数重构
     * @param update     更新注解
     * @param params     参数
     * @param returnType 返回参数
     */
    public void update(MethodSpec.Builder builder, ExecutableElement method, Update update, List<? extends VariableElement> params, TypeMirror returnType)
    {
        builder.addStatement("throw new $T($S)", RuntimeException.class, "函数尚未实现");
    }

    /**
     * 查询
     *
     * @param builder    函数重构
     * @param query      查询注解
     * @param params     参数
     * @param returnType 返回参数
     */
    public void query(MethodSpec.Builder builder, ExecutableElement method, Query query, List<? extends VariableElement> params, TypeMirror returnType)
    {
        builder.addStatement("throw new $T($S)", RuntimeException.class, "函数尚未实现");
    }
}
