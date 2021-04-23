package me.xiaolei.room_lite.compiler.utils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.Filer;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import me.xiaolei.room_lite.EntityHelper;
import me.xiaolei.room_lite.annotations.Entity;
import me.xiaolei.room_lite.annotations.Limit;
import me.xiaolei.room_lite.annotations.OrderBy;
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
            if (TypeUtil.isEntityArray(paramType)) // 数组
            {
                entityType = ((ArrayType) paramType).getComponentType();
            } else if (TypeUtil.isEntityList(paramType)) // List集合
            {
                List<? extends TypeMirror> typeArgs = ((DeclaredType) paramType).getTypeArguments();
                entityType = typeArgs.get(0);
            } else if (TypeUtil.isEntity(paramType)) // Entity
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
        Set<TypeElement> entities = new HashSet<>();
        for (VariableElement param : params)
        {
            // 参数类型
            TypeMirror paramType = param.asType();
            // 参数名称
            String paramName = param.getSimpleName().toString();
            TypeMirror entityType = null;
            if (TypeUtil.isEntityArray(paramType)) // Entity数组
            {
                entityType = ((ArrayType) paramType).getComponentType();
            } else if (TypeUtil.isEntityList(paramType)) // Entity集合
            {
                List<? extends TypeMirror> typeArgs = ((DeclaredType) paramType).getTypeArguments();
                entityType = typeArgs.get(0);
            } else if (TypeUtil.isEntity(paramType)) // 单个Entity
            {
                entityType = paramType;
            }

            TypeElement entityElement = (TypeElement) typeUtil.asElement(entityType);
            entities.add(entityElement);
            String entityTypeName = entityElement.getQualifiedName().toString();
            String helperName = paramName + "$$helper";

            if (TypeUtil.isEntityArray(paramType) || TypeUtil.isEntityList(paramType)) // List和数组的代码方式一样
            {
                builder.addCode("for($N obj:$N)", entityTypeName, paramName);
                builder.addCode("{");
                builder.addStatement("changeCount.getAndAdd((int) $N.insert(transaction, obj))", helperName);
                builder.addCode("}");
            } else if (TypeUtil.isEntity(paramType))
            {
                builder.addStatement("changeCount.getAndAdd((int) $N.insert(transaction, $N))", helperName, paramName);
            }
        }
        builder.addStatement("})");
        // 批量生成通知表更新事件的代码
        for (TypeElement entity : entities)
        {
            builder.addStatement("this.database.notifyTable($S)", ElementUtil.getTableName(entity));
        }
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
        // 首先检查返回类型合不合法
        TypeKind returnTypeKind = returnType.getKind();
        if (returnTypeKind != TypeKind.INT && returnTypeKind != TypeKind.VOID)
        {
            throw new RuntimeException(method + "@Delete 注解支持的返回类型为: int void");
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
            if (TypeUtil.isEntityArray(paramType)) // 数组
            {
                entityType = ((ArrayType) paramType).getComponentType();
            } else if (TypeUtil.isEntityList(paramType)) // List集合
            {
                List<? extends TypeMirror> typeArgs = ((DeclaredType) paramType).getTypeArguments();
                entityType = typeArgs.get(0);
            } else if (TypeUtil.isEntity(paramType)) // Entity
            {
                entityType = paramType;
            } else
            {
                throw new RuntimeException(method + "@Delete 支持的参数类型为: Entity / Entity[] / List<Entity>");
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

        Set<TypeElement> entities = new HashSet<>();
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
            if (TypeUtil.isEntityArray(paramType)) // Entity数组
            {
                entityType = ((ArrayType) paramType).getComponentType();
            } else if (TypeUtil.isEntityList(paramType)) // Entity集合
            {
                List<? extends TypeMirror> typeArgs = ((DeclaredType) paramType).getTypeArguments();
                entityType = typeArgs.get(0);
            } else if (TypeUtil.isEntity(paramType)) // 单个Entity
            {
                entityType = paramType;
            }

            TypeElement entityElement = (TypeElement) typeUtil.asElement(entityType);
            entities.add(entityElement);
            String entityTypeName = entityElement.getQualifiedName().toString();
            String helperName = paramName + "$$helper";

            if (TypeUtil.isEntityArray(paramType) || TypeUtil.isEntityList(paramType)) // List和数组的代码方式一样
            {
                builder.addCode("for($N obj:$N)", entityTypeName, paramName);
                builder.addCode("{");
                builder.addStatement("changeCount.getAndAdd((int) $N.delete(transaction, obj))", helperName);
                builder.addCode("}");
            } else if (TypeUtil.isEntity(paramType))
            {
                builder.addStatement("changeCount.getAndAdd((int) $N.delete(transaction, $N))", helperName, paramName);
            }
        }
        builder.addStatement("})");
        // 批量生成通知表更新事件的代码
        for (TypeElement entity : entities)
        {
            builder.addStatement("this.database.notifyTable($S)", ElementUtil.getTableName(entity));
        }
        // 判断是否需要返回int数据
        if (returnTypeKind == TypeKind.INT)
        {
            builder.addStatement("return changeCount.get()");
        }
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
        // 首先检查返回类型合不合法
        TypeKind returnTypeKind = returnType.getKind();
        if (returnTypeKind != TypeKind.INT && returnTypeKind != TypeKind.VOID)
        {
            throw new RuntimeException(method + "@Update 注解支持的返回类型为: int void");
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
            if (TypeUtil.isEntityArray(paramType)) // 数组
            {
                entityType = ((ArrayType) paramType).getComponentType();
            } else if (TypeUtil.isEntityList(paramType)) // List集合
            {
                List<? extends TypeMirror> typeArgs = ((DeclaredType) paramType).getTypeArguments();
                entityType = typeArgs.get(0);
            } else if (TypeUtil.isEntity(paramType)) // Entity
            {
                entityType = paramType;
            } else
            {
                throw new RuntimeException(method + "@Update 支持的参数类型为: Entity / Entity[] / List<Entity>");
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

        Set<TypeElement> entities = new HashSet<>();
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
            if (TypeUtil.isEntityArray(paramType)) // Entity数组
            {
                entityType = ((ArrayType) paramType).getComponentType();
            } else if (TypeUtil.isEntityList(paramType)) // Entity集合
            {
                List<? extends TypeMirror> typeArgs = ((DeclaredType) paramType).getTypeArguments();
                entityType = typeArgs.get(0);
            } else if (TypeUtil.isEntity(paramType)) // 单个Entity
            {
                entityType = paramType;
            }

            TypeElement entityElement = (TypeElement) typeUtil.asElement(entityType);
            entities.add(entityElement);
            String entityTypeName = entityElement.getQualifiedName().toString();
            String helperName = paramName + "$$helper";

            if (TypeUtil.isEntityArray(paramType) || TypeUtil.isEntityList(paramType)) // List和数组的代码方式一样
            {
                builder.addCode("for($N obj:$N)", entityTypeName, paramName);
                builder.addCode("{");
                builder.addStatement("changeCount.getAndAdd((int) $N.update(transaction, obj))", helperName);
                builder.addCode("}");
            } else if (TypeUtil.isEntity(paramType))
            {
                builder.addStatement("changeCount.getAndAdd((int) $N.update(transaction, $N))", helperName, paramName);
            }
        }
        builder.addStatement("})");
        // 批量生成通知表更新事件的代码
        for (TypeElement entity : entities)
        {
            builder.addStatement("this.database.notifyTable($S)", ElementUtil.getTableName(entity));
        }
        // 判断是否需要返回int数据
        if (returnTypeKind == TypeKind.INT)
        {
            builder.addStatement("return changeCount.get()");
        }
    }

    /**
     * 查询
     *
     * @param builder    函数重构
     * @param query      查询注解
     * @param params     参数
     * @param returnType 返回参数
     */
    public void query(TypeSpec.Builder implClass, MethodSpec.Builder builder, ExecutableElement method, Query query, List<? extends VariableElement> params, TypeMirror returnType)
    {
        AnnotationMirror annotationMirrors = method.getAnnotationMirrors().stream().filter((mirror ->
        {
            return mirror.getAnnotationType().toString().equals(Query.class.getCanonicalName());
        })).findFirst().get();
        AnnotationValue entityValue = annotationMirrors.getElementValues().entrySet().stream().filter((entry) ->
        {
            return entry.getKey().getSimpleName().toString().equals("entity");
        }).findFirst().get().getValue();
        // 查询语句建造
        StringBuilder sqlBuilder = new StringBuilder();
        // 查询什么
        String what = query.what();
        // 从哪里查
        TypeMirror entityClass = (TypeMirror) entityValue.getValue();
        Element entityElement = typeUtil.asElement(entityClass);
        // 判断这个entity传过来的class是不是@Entity对应的class
        if (entityElement.getAnnotation(Entity.class) == null)
            throw new RuntimeException(method + " @Query对应的entity，传入的class必须是@Entity修饰的表类");
        String tableName = ElementUtil.getTableName(typeUtil.asElement(entityClass));
        // 查询条件
        String where = query.whereClause();
        // 翻页条件
        Limit limit = query.limit();
        // 分组
        String[] groupBy = query.groupBy();
        // 排序
        OrderBy orderBy = query.orderBy();

        // 生成查询语句
        sqlBuilder.append("SELECT ")
                .append(what)
                .append(" FROM ")
                .append(tableName)
                .append(" WHERE ")
                .append(where);
        // groupBy
        if (groupBy.length > 0)
        {
            StringJoiner groupByJoiner = new StringJoiner(",");
            for (String columnName : groupBy)
            {
                if (columnName.isEmpty())
                    throw new RuntimeException(method + " groupBy 中，字符串不能为空字符串");
                groupByJoiner.add(columnName);
            }
            sqlBuilder.append(" GROUP BY ").append(groupByJoiner.toString());
        }
        // orderBy
        if (orderBy.columnNames().length > 0)
        {
            StringJoiner orderByJoiner = new StringJoiner(",");
            String[] columnNames = orderBy.columnNames();
            for (String columnName : columnNames)
            {
                if (columnName.isEmpty())
                    throw new RuntimeException(method + " orderBy.columnNames 中，字符串不能为空字符串");
                orderByJoiner.add(columnName);
            }
            sqlBuilder.append(" ORDER BY ").append(orderByJoiner.toString()).append(" ").append(orderBy.type());
        }
        // limit
        if (!limit.index().isEmpty() && !limit.maxLength().isEmpty())
        {
            Pattern pattern = Pattern.compile("([0-9]+)|\\?");
            String index = limit.index();
            String maxLength = limit.maxLength();
            Matcher indexMatcher = pattern.matcher(index);
            Matcher maxLengthMatcher = pattern.matcher(maxLength);
            if (!indexMatcher.matches())
            {
                throw new RuntimeException(method + "" + limit + " index的值必须是数字或者是?占位符");
            }
            if (!maxLengthMatcher.matches())
            {
                throw new RuntimeException(method + "" + limit + " maxLength的值必须是数字或者是?占位符");
            }
            sqlBuilder.append(" LIMIT ").append(limit.index()).append(",").append(limit.maxLength());
        }
        String querySql = sqlBuilder.toString();
        // 拼接参数
        StringJoiner joiner = new StringJoiner(",");
        for (VariableElement param : params)
        {
            String paramName = param.getSimpleName().toString();
            joiner.add("String.valueOf(" + paramName + ")");
        }
        String argsName = method.getSimpleName().toString() + "$$args";
        // 自动生成参数代码
        builder.addStatement("$T[] $N = new String[]{" + joiner + "}", String.class, argsName);


        if (TypeUtil.isArray(returnType)) // 是数组
        {
            // 生成查询结果代码 try cache
            builder.addCode("try($T cursor = this.sqLite.rawQuery($S, $N))", Global.Cursor, querySql, argsName);
            builder.addCode("{");
            // 获取数组的类型
            TypeMirror componentType = ((ArrayType) returnType).getComponentType();
            builder.addStatement("$T[] results = new $T[cursor.getCount()]", ClassName.get(componentType), ClassName.get(componentType));//;
            queryBase(builder, entityClass, componentType);
            builder.addStatement("results[cursor.getPosition()] = value");
            builder.addCode("}");
            builder.addStatement("return results");
            // catch
            builder.addCode("}catch ($T e){throw new $T(e);}", Exception.class, RuntimeException.class);
        } else if (TypeUtil.isList(returnType)) // List
        {
            DeclaredType type = ((DeclaredType) returnType);
            List<? extends TypeMirror> typeArguments = type.getTypeArguments();
            if (typeArguments.isEmpty())
            {
                throw new RuntimeException(method + " List必须加入准确的类型");
            }
            // 获取泛型的类型
            TypeMirror componentType = typeArguments.get(0);
            if (componentType instanceof WildcardType)
            {
                throw new RuntimeException(method + " List不支持通配符");
            }
            // 生成查询结果代码 try cache
            builder.addCode("try($T cursor = this.sqLite.rawQuery($S, $N))", Global.Cursor, querySql, argsName);
            builder.addCode("{");
            builder.addStatement("$T<$T> results = new $T()", List.class, ClassName.get(componentType), ArrayList.class);
            queryBase(builder, entityClass, componentType);
            builder.addStatement("results.add(value)");
            builder.addCode("}");
            builder.addStatement("return results");
            // catch
            builder.addCode("}catch ($T e){throw new $T(e);}", Exception.class, RuntimeException.class);
        } else if (TypeUtil.isPrimitiveOrBox(returnType) || returnType.equals(entityClass)) // 是基础类型
        {
            // 生成查询结果代码 try cache
            builder.addCode("try($T cursor = this.sqLite.rawQuery($S, $N))", Global.Cursor, querySql, argsName);
            builder.addCode("{");
            queryBase(builder, entityClass, returnType);
            builder.addStatement("return value");
            builder.addCode("}");
            builder.addStatement("return ($T) $T.defaultValue($T.class)", ClassName.get(returnType), Global.RoomLiteUtil, ClassName.get(returnType));
            // catch
            builder.addCode("}catch ($T e){throw new $T(e);}", Exception.class, RuntimeException.class);
        } else if (returnType instanceof DeclaredType)
        {
            DeclaredType type = ((DeclaredType) returnType);
            List<? extends TypeMirror> typeArguments = type.getTypeArguments();
            if (typeArguments.size() == 1) // 有一个泛型,在这里其实就是可以对一些扩展进行支持，譬如：RxJava LiveData
            {
                TypeMirror generic = typeArguments.get(0);
                TypeElement returnElement = (TypeElement) typeUtil.asElement(type);
                // 生成需要真正执行的函数名称
                String invokeMethodName = returnElement.getSimpleName().toString() + "$$" + method.getSimpleName().toString() + "$$Adapter";
                // 首先生成真正执行的函数的代码
                implClass.addMethod(createAdapterMethod(invokeMethodName, generic, entityClass, querySql));
                // 再生成调用函数的代码
                builder.addStatement("$T adapter = $T.getAdapter($T.class)", Global.Adapter, Global.Adapters, ClassName.get(returnElement));

                builder.addStatement("$T sql = $S", String.class, querySql);
                builder.addCode("$T processor = new $T(this.database, this.sqLite,$S, sql,$N)", Global.Processor, Global.Processor, tableName, argsName);//
                builder.addCode("{");
                builder.addCode("@Override\n");
                builder.addCode("public Object cursorToObject($T cursor)", Global.Cursor);
                builder.addCode("{");
                builder.addStatement("return $N(cursor)", invokeMethodName);
                builder.addCode("}");
                builder.addStatement("}");
                builder.addStatement("return ($T) adapter.process(processor,$T.class)", ClassName.get(type), ClassName.get(typeUtil.erasure(generic)));
            } else
            {
                // 生成查询结果代码 try cache
                builder.addCode("try($T cursor = this.sqLite.rawQuery($S, $N))", Global.Cursor, querySql, argsName);
                builder.addCode("{");
                queryBase(builder, entityClass, returnType);
                builder.addStatement("return value");
                builder.addCode("}");
                builder.addStatement("return ($T)$T.defaultValue($T.class)", ClassName.get(returnType), Global.RoomLiteUtil, ClassName.get(returnType));
                // catch
                builder.addCode("}catch ($T e){throw new $T(e);}", Exception.class, RuntimeException.class);
            }
        } else
        {
            builder.addStatement("throw new $T($S)", RuntimeException.class, "RooLite尚未实现对" + returnType + "的支持");
        }
    }

    /**
     * 生成Adapter执行的函数
     */
    private MethodSpec createAdapterMethod(String methodName, TypeMirror returnType, TypeMirror entityClass, String querySql)
    {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Global.Cursor, "cursor")
                .returns(ClassName.get(returnType));

        if (TypeUtil.isArray(returnType)) // 是数组
        {
            // 获取数组的类型
            TypeMirror componentType = ((ArrayType) returnType).getComponentType();
            builder.addStatement("$T[] results = new $T[cursor.getCount()]", ClassName.get(componentType), ClassName.get(componentType));//;
            queryBase(builder, entityClass, componentType);
            builder.addStatement("results[cursor.getPosition()] = value");
            builder.addCode("}");
            builder.addStatement("return results");
        } else if (TypeUtil.isList(returnType)) // List
        {
            DeclaredType type = ((DeclaredType) returnType);
            List<? extends TypeMirror> typeArguments = type.getTypeArguments();
            if (typeArguments.isEmpty())
            {
                throw new RuntimeException(" List必须加入准确的类型");
            }
            // 获取泛型的类型
            TypeMirror componentType = typeArguments.get(0);
            if (componentType instanceof WildcardType)
            {
                throw new RuntimeException(" List不支持通配符");
            }
            // 生成查询结果代码 try cache
            builder.addStatement("$T<$T> results = new $T()", List.class, ClassName.get(componentType), ArrayList.class);
            queryBase(builder, entityClass, componentType);
            builder.addStatement("results.add(value)");
            builder.addCode("}");
            builder.addStatement("return results");
        } else if (TypeUtil.isPrimitiveOrBox(returnType) || returnType.equals(entityClass)) // 是基础类型
        {
            // 生成查询结果代码 try cache
            queryBase(builder, entityClass, returnType);
            builder.addStatement("return value");
            builder.addCode("}");
            builder.addStatement("return ($T) $T.defaultValue($T.class)", ClassName.get(returnType), Global.RoomLiteUtil, ClassName.get(returnType));
            // catch
        } else if (returnType instanceof DeclaredType)
        {
            // 生成查询结果代码 try cache
            queryBase(builder, entityClass, returnType);
            builder.addStatement("return value");
            builder.addCode("}");
            builder.addStatement("return ($T)$T.defaultValue($T.class)", ClassName.get(returnType), Global.RoomLiteUtil, ClassName.get(returnType));
            // catch
        } else
        {
            builder.addStatement("throw new $T($S)", RuntimeException.class, "RooLite尚未实现对" + returnType + "的支持");
        }
        return builder.build();
    }

    /**
     * 自动生成查询到cursor之后的转换的代码
     */
    private static void queryBase(MethodSpec.Builder builder, TypeMirror entityClass, TypeMirror componentType)
    {
        // 判断数据类型是不是基础类型,或者是字符串
        if (TypeUtil.isPrimitiveOrBox(componentType) || TypeUtil.isString(componentType))
        {
            builder.addStatement("String[] columnNames = cursor.getColumnNames()");
            builder.addStatement("int columnIndex = cursor.getColumnIndex(columnNames[0])");
            builder.addStatement("$T convert = $T.getConvert($T.class)", Global.Convert, Global.Converts, ClassName.get(componentType));
            builder.addCode("while (cursor.moveToNext()) {");
            builder.addStatement("$T value = ($T) convert.cursorToJavaObject(cursor, columnIndex)", ClassName.get(componentType), ClassName.get(componentType));
        } else if (componentType.equals(entityClass)) // 再判断类型是不是当前表的类型
        {
            builder.addStatement("$T helper = this.database.getEntityHelper($T.class)", EntityHelper.class, ClassName.get(componentType));//;
            builder.addCode("while (cursor.moveToNext()) {");//
            builder.addStatement("$T value = ($T) helper.fromCursor(cursor)", ClassName.get(componentType), ClassName.get(componentType));//
        } else
        {
            // 数组不是基础类型，也不是表的类型
            builder.addStatement("String[] columnNames = cursor.getColumnNames()");
            builder.addStatement("int columnIndex = cursor.getColumnIndex(columnNames[0])");
            builder.addStatement("$T convert = $T.getConvert($T.class)", Global.Convert, Global.Converts, ClassName.get(componentType));
            builder.addStatement("if (convert == null) throw new $T($S)", RuntimeException.class, "函数尚未实现对" + componentType + "的支持");
            builder.addCode("while (cursor.moveToNext()) {");
            builder.addStatement("$T value = ($T) convert.cursorToJavaObject(cursor, columnIndex)", ClassName.get(componentType), ClassName.get(componentType));
        }
    }
}
