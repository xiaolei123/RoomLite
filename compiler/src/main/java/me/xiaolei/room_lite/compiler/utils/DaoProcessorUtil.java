package me.xiaolei.room_lite.compiler.utils;

import com.squareup.javapoet.MethodSpec;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import me.xiaolei.room_lite.EntityHelper;
import me.xiaolei.room_lite.annotations.Entity;
import me.xiaolei.room_lite.annotations.dao.Delete;
import me.xiaolei.room_lite.annotations.dao.Insert;
import me.xiaolei.room_lite.annotations.dao.Query;
import me.xiaolei.room_lite.annotations.dao.Update;
import me.xiaolei.room_lite.compiler.Global;

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
        // 首先检查返回类型合不合法
        TypeKind returnTypeKind = returnType.getKind();
        if (returnTypeKind != TypeKind.INT && returnTypeKind != TypeKind.VOID)
        {
            throw new RuntimeException(method + "@Insert 注解支持的返回类型为: int void");
        }

        builder.addStatement("$T changeCount = new AtomicInteger(0)", AtomicInteger.class);
        // 先循环参数，循环生成类型判断代码
        for (VariableElement param : params)
        {
            // 参数类型
            TypeMirror paramType = param.asType();
            // 参数名称
            String paramName = param.getSimpleName().toString();
            TypeMirror entityType;
            // 抽取插入的类型
            if (isEntityArray(paramType)) // 数组
            {
                entityType = ((ArrayType) paramType).getComponentType();
            } else if (isEntityList(paramType)) // List集合
            {
                List<? extends TypeMirror> typeArgs = ((DeclaredType) paramType).getTypeArguments();
                entityType = typeArgs.get(0);
            } else if (isEntity(paramType)) // Entity
            {
                entityType = paramType;
            } else
            {
                throw new RuntimeException(method + "@Insert 支持的参数类型为: Entity / Entity[] / List<Entity>");
            }
            // 插入类型的Element
            TypeElement entityElement = (TypeElement) typeUtil.asElement(entityType);
            // 获取到对应的类全名
            String entityTypeName = entityElement.getQualifiedName().toString();
            // 定义对应的帮助类的名称
            String helperName = paramName + "$$helper";
            // 获取对应的帮助类
            builder.addStatement("$T $N = this.database.getEntityHelper($N.class)", EntityHelper.class, helperName, entityTypeName);
            // 对帮助类进行判断，不存在则说明插入的类型不属于这个数据库
            builder.addCode("if ($N == null)", helperName);
            // 直接抛出异常
            builder.addStatement("throw new $T(this.database.getDatabaseName() + \"不存在 $N所对应的表\")", RuntimeException.class, entityTypeName);
        }

        // 再循环参数，循环生成批量插入代码
        builder.addCode("this.sqLite.doTransaction((transaction) ->");//
        builder.addCode("{");//
        for (VariableElement param : params)
        {
            // 参数类型
            TypeMirror paramType = param.asType();
            // 参数名称
            String paramName = param.getSimpleName().toString();
            TypeMirror entityType = null;
            if (isEntityArray(paramType)) // Entity数组
            {
                entityType = ((ArrayType) paramType).getComponentType();
            } else if (isEntityList(paramType)) // Entity集合
            {
                List<? extends TypeMirror> typeArgs = ((DeclaredType) paramType).getTypeArguments();
                entityType = typeArgs.get(0);
            } else if (isEntity(paramType)) // 单个Entity
            {
                entityType = paramType;
            }

            TypeElement entityElement = (TypeElement) typeUtil.asElement(entityType);
            String tableName = ElementUtil.getTableName(entityElement);
            String entityTypeName = entityElement.getQualifiedName().toString();
            String helperName = paramName + "$$helper";
            String valueName = paramName + "$$values";

            if (isEntityArray(paramType) || isEntityList(paramType)) // List和数组的代码方式一样
            {
                builder.addCode("for($N obj:$N)", entityTypeName, paramName);
                builder.addCode("{");
                builder.addStatement("$T $N = $N.toContentValues(obj)", Global.ContentValues, valueName, helperName);
                builder.addStatement("changeCount.getAndAdd((int) transaction.insert($S, null, $N))", tableName, valueName);
                builder.addCode("}");
            } else if (isEntity(paramType))
            {
                builder.addStatement("$T $N = $N.toContentValues($N)", Global.ContentValues, valueName, helperName, paramName);
                builder.addStatement("changeCount.getAndAdd((int) transaction.insert($S, null, $N))", tableName, valueName);
            }
        }
        builder.addStatement("})");

        // 判断是否需要返回int数据
        if (returnTypeKind == TypeKind.INT)
        {
            builder.addStatement("return changeCount.get()");
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
