package me.xiaolei.room_lite.compiler.utils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import me.xiaolei.room_lite.ConflictAlgorithm;
import me.xiaolei.room_lite.Suffix;
import me.xiaolei.room_lite.annotations.Column;
import me.xiaolei.room_lite.annotations.Entity;
import me.xiaolei.room_lite.annotations.Index;
import me.xiaolei.room_lite.annotations.PrimaryKey;
import me.xiaolei.room_lite.compiler.Global;

/**
 * 生成代码的工具
 */
public class EntityHelperUtils
{

    /**
     * 检查表是否合法
     */
    public static void checkEntityLegitimate(TypeElement element) throws Exception
    {
        String type = element.asType().toString();
        Entity entity = element.getAnnotation(Entity.class);
        if (entity == null)
        {
            throw new Exception(type + "必须使用@" + Entity.class.getComponentType() + "进行注解");
        }
        List<VariableElement> allFields = ElementUtil.getFields(element);
        if (!ElementUtil.hasPrimaryKey(allFields))
        {
            throw new Exception(type + "必须至少有一个主键@" + PrimaryKey.class);
        }
    }

    /**
     * 获取主键数组名称数组
     */
    public static FieldSpec keyNames(TypeElement element)
    {
        FieldSpec.Builder builder = FieldSpec.builder(String[].class, "keyNames")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        // 获取所有的字段
        List<VariableElement> fields = ElementUtil.getFields(element);
        // 定义容器，存放主键字段
        List<VariableElement> keyFields = new LinkedList<>();
        // 过滤主键字段
        for (VariableElement field : fields)
        {
            if (ElementUtil.isPrimaryKey(field))
            {
                keyFields.add(field);
            }
        }
        // 字符串占位符拼接
        StringJoiner joiner = new StringJoiner(",");
        // 对应的占位符的数据
        Object[] values = new Object[keyFields.size() + 1];
        // 第一个占位符是代表类型为字符串类型
        values[0] = String.class;
        for (int i = 0; i < keyFields.size(); i++)
        {
            VariableElement field = keyFields.get(i);
            // 获取字段的数据库字段名称
            String columnName = ElementUtil.getColumnName(field);
            // 保存数据库字段名称
            values[i + 1] = columnName;
            // 拼接占位符
            joiner.add("$S");
        }
        builder.initializer("new $T[]{" + joiner + "}", values);

        return builder.build();
    }

    /**
     * 对字段进行解析，并且生成对应的解析器的字段
     */
    public static FieldSpec[] convertAndConstructor(MethodSpec.Builder constructor, TypeElement element)
    {
        List<VariableElement> fields = ElementUtil.getFields(element);
        // 为每一个字段，自动映射一个对应的Convert
        FieldSpec[] fieldSpecs = new FieldSpec[fields.size()];
        for (int i = 0; i < fields.size(); i++)
        {
            VariableElement field = fields.get(i);
            // 字段名称
            String fieldName = field.getSimpleName().toString();
            // 转换器字段名称
            String convertFieldName = fieldName + Suffix.convert_suffix;
            // 在构造函数里进行获取
            constructor.addStatement("this.$N = $T.getConvert($T.class)", convertFieldName, Global.Converts, field.asType());
            // 判断获取的转换器，如果没有获取到，则抛出异常
            constructor.addStatement("if ($N == null) throw new $T($S)", convertFieldName, RuntimeException.class, field.asType() + "对应的转换器Convert未定义");
            fieldSpecs[i] = FieldSpec.builder(Global.Convert, convertFieldName, Modifier.PUBLIC)
                    .addModifiers(Modifier.FINAL)
                    .build();
        }
        return fieldSpecs;
    }

    /**
     * 解析表名的方法
     *
     * @param element
     * @return
     */
    public static MethodSpec getTableName(TypeElement element)
    {
        String tableName = ElementUtil.getTableName(element);
        return MethodSpec.methodBuilder("getTableName")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("return $S", tableName)
                .returns(Global.String)
                .build();
    }

    /**
     * 解析生成SQL语句的方法
     *
     * @param element
     * @return
     */
    public static MethodSpec getCreateSQL(TypeElement element)
    {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getCreateSQL")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(Global.String);
        // 获取所有字段
        List<VariableElement> fields = ElementUtil.getFields(element);
        // 获取表名
        String tableName = ElementUtil.getTableName(element);
        // 构建语句
        builder.addStatement("$T sql = $S", String.class, "CREATE TABLE IF NOT EXISTS ");
        builder.addStatement("sql += $S", tableName);
        builder.addStatement("sql += $S", "(");
        for (int i = 0; i < fields.size(); i++)
        {
            VariableElement field = fields.get(i);
            // 当前字段对应的Java类型
            String fieldType = field.asType().toString();
            // 获取当前的字段的对应的Column
            Column column = field.getAnnotation(Column.class);
            // 获取数据库字段的名称
            String columnName = ElementUtil.getColumnName(field);
            // 生成Column.SQLType 字段的名称
            String sqlTypeName = columnName + "$$sqlType";
            // 防止字段冲突，对字段使用 `` 进行包裹
            String columnNameBox = "`" + columnName + "` ";
            // 根据当前下标，决定结尾部分是否有逗号分割
            String endStatement = (i < fields.size() - 1) ? "," : "";
            if (column != null && column.type() != Column.SQLType.UNDEFINED)
            {
                // 生成代码，获取当前的SQLType的值，并生成进去
                builder.addStatement("$T $N = Column.SQLType.$N", Column.SQLType.class, sqlTypeName, column.type().name());
            } else
            {
                // 生成代码，获取当前的字段类型，从Converts里获取对应的 SQLType 
                builder.addStatement("$T $N = Converts.convertSqlType($N.class)", Column.SQLType.class, sqlTypeName, fieldType);
            }
            // 根据当前字段，获取字段的其他语句标签
            String tag = ElementUtil.getColumnTag(field);
            // SQL语句添加
            builder.addStatement("sql += $S + $N.getTypeString() + $S + $S", columnNameBox, sqlTypeName, tag, endStatement);
        }
        // sqlBuilder.append(")");
        builder.addStatement("sql += $S", ")");
        builder.addStatement("return sql");
        return builder.build();
    }

    /**
     * 获取所有索引的名称
     */
    public static MethodSpec indexNames(TypeElement element)
    {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("indexNames")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(String[].class);

        // 获取表名
        String tableName = ElementUtil.getTableName(element);
        // 获取@Entity的注解
        Entity entity = element.getAnnotation(Entity.class);
        // 获取所有字段
        List<VariableElement> fields = ElementUtil.getFields(element);
        // 获取表注解上的索引
        Index[] indices = entity.indices();
        // 获取所有字段上的，并且加了索引的注解
        List<VariableElement> columns = fields.stream().filter(varElement ->
        {
            Column column = varElement.getAnnotation(Column.class);
            return column != null && column.index();
        }).collect(Collectors.toList());
        // 获取索引的数量
        int indexCount = columns.size() + indices.length;
        int index = 0;
        // 生成对应数量的预备字符串占位
        builder.addStatement("$T[] indexNames = new $T[$L]", String.class, String.class, indexCount);
        ArrayList<String> indexNames = new ArrayList<>();
        // 对字段上的注解进行索引SQL语句的生成
        for (VariableElement columnElement : columns)
        {
            String columnName = ElementUtil.getColumnName(columnElement);
            String indexName = tableName + "_" + columnName + "_index";
            // 先判断这个索引名称是否存在
            if (indexNames.contains(indexName))
            {
                throw new RuntimeException(element + "声明的索引: " + indexName + " 有重复");
            } else
            {
                // 如果不存在，则缓存起来
                indexNames.add(indexName);
            }
            builder.addStatement("indexNames[$L] = $S", index++, indexName);
        }
        // 对表注解上的索引进行循环解析SQL语句的生成
        for (Index t_index : indices)
        {
            // 字段名称
            String[] columnNames = t_index.columnNames();
            if (columnNames.length == 0)
            {
                throw new RuntimeException(element + "使用的@Entity里，indices 里，索引的字段名称不许为空");
            }
            // 索引名称
            String indexName = t_index.name().isEmpty() ? (tableName + "_index") : t_index.name();

            // 先判断这个索引名称是否存在
            if (indexNames.contains(indexName))
            {
                throw new RuntimeException(element + "声明的索引: " + indexName + " 有重复");
            } else
            {
                // 如果不存在，则缓存起来
                indexNames.add(indexName);
            }
            // 生成索引部分代码
            builder.addStatement("indexNames[$L] = $S", index++, indexName);
        }
        builder.addStatement("return indexNames");
        return builder.build();
    }

    /**
     * 获取所有的字段名称
     */
    public static MethodSpec columnNames(TypeElement element)
    {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("columnNames")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(String[].class);
        // 获取所有字段
        List<VariableElement> fields = ElementUtil.getFields(element);
        int index = 0;
        builder.addStatement("$T[] columnNames = new $T[$L]", String.class, String.class, fields.size());
        for (int i = 0; i < fields.size(); i++)
        {
            VariableElement field = fields.get(i);
            String columnName = ElementUtil.getColumnName(field);
            builder.addStatement("columnNames[$L] = $S", index++, columnName);
        }
        builder.addStatement("return columnNames");
        return builder.build();
    }

    /**
     * 创建索引
     */
    public static MethodSpec getCreateIndexSQL(TypeElement element)
    {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getCreateIndexSQL")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(String[].class);
        // 获取表名
        String tableName = ElementUtil.getTableName(element);
        // 获取@Entity的注解
        Entity entity = element.getAnnotation(Entity.class);
        // 获取所有字段
        List<VariableElement> fields = ElementUtil.getFields(element);
        // 获取表注解上的索引
        Index[] indices = entity.indices();
        // 获取所有字段上的，并且加了索引的注解
        List<VariableElement> columns = fields.stream().filter(varElement ->
        {
            Column column = varElement.getAnnotation(Column.class);
            return column != null && column.index();
        }).collect(Collectors.toList());
        // 获取索引的数量
        int indexCount = columns.size() + indices.length;
        int index = 0;
        // 生成对应数量的预备字符串占位
        builder.addStatement("$T[] sqls = new $T[$L]", String.class, String.class, indexCount);
        ArrayList<String> indexNames = new ArrayList<>();
        // 对字段上的注解进行索引SQL语句的生成
        for (VariableElement columnElement : columns)
        {
            boolean unique = columnElement.getAnnotation(Column.class).unique();
            String columnName = ElementUtil.getColumnName(columnElement);
            String indexName = tableName + "_" + columnName + "_index";
            // 先判断这个索引名称是否存在
            if (indexNames.contains(indexName))
            {
                throw new RuntimeException(element + "声明的索引: " + indexName + " 有重复");
            } else
            {
                // 如果不存在，则缓存起来
                indexNames.add(indexName);
            }

            StringBuilder indexSql = new StringBuilder()
                    .append("CREATE ");
            if (unique)
            {
                indexSql.append("UNIQUE ");
            }
            indexSql.append("INDEX ")
                    .append(indexName)
                    .append(" ON ")
                    .append(tableName)
                    .append("(`")
                    .append(columnName)
                    .append("`);");
            builder.addStatement("sqls[$L] = $S", index++, indexSql.toString());
        }
        // 对表注解上的索引进行循环解析SQL语句的生成
        for (Index t_index : indices)
        {
            // 字段名称
            String[] columnNames = t_index.columnNames();
            if (columnNames.length == 0)
            {
                throw new RuntimeException(element + "使用的@Entity里，indices 里，索引的字段名称不许为空");
            }
            // 索引名称
            String indexName = t_index.name().isEmpty() ? (tableName + "_index") : t_index.name();

            // 先判断这个索引名称是否存在
            if (indexNames.contains(indexName))
            {
                throw new RuntimeException(element + "声明的索引: " + indexName + " 有重复");
            } else
            {
                // 如果不存在，则缓存起来
                indexNames.add(indexName);
            }
            // 是否唯一
            boolean unique = t_index.unique();
            // 构建加入索引的字段名
            StringJoiner joiner = new StringJoiner(",");
            // 加入索引的字段名
            for (String columnName : columnNames)
            {
                joiner.add("`" + columnName + "`");
            }
            // SQL语句
            StringBuilder indexSql = new StringBuilder().append("CREATE ");
            // 是否唯一
            if (unique)
            {
                indexSql.append("UNIQUE ");
            }
            // 构建SQL语句
            indexSql.append("INDEX ")
                    .append(indexName)
                    .append(" ON ")
                    .append(tableName)
                    .append("(")
                    .append(joiner)
                    .append(");");
            // 生成索引部分代码
            builder.addStatement("sqls[$L] = $S", index++, indexSql.toString());
        }

        builder.addStatement("return sqls");

        return builder.build();
    }

    /**
     * 创建生成对象的
     */
    public static MethodSpec fromCursor(TypeElement element)
    {
        // 获取所有的字段
        List<VariableElement> fields = ElementUtil.getFields(element);

        String klassName = element.getQualifiedName().toString();
        MethodSpec.Builder fromCursor = MethodSpec.methodBuilder("fromCursor")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(Global.Cursor, "cursor")
                .addStatement(klassName + " obj = new " + klassName + "()");
        // 使用对应的Convert转换设置对象的属性值
        for (VariableElement field : fields)
        {
            // 数据库字段名称
            String columnName = ElementUtil.getColumnName(field);
            // 类字段类型
            String fieldType = field.asType().toString();
            // 类字段名称
            String fieldName = field.getSimpleName().toString();
            // 对应的转换器的名字
            String convertName = fieldName + Suffix.convert_suffix;
            // 首先获取数据库的ColumnIndex
            fromCursor.addStatement("int $N_Index = cursor.getColumnIndex($S)", columnName, columnName);
            // 判断查询结果中是否存在这个值
            fromCursor.addCode("if ($N_Index != -1)", columnName);
            // 使用转换器去获取对应的值，并强转成对应的字段类型
            fromCursor.addStatement("obj.$N = ($N)this.$N.cursorToJavaObject(cursor,$N_Index)", field.getSimpleName(), fieldType, convertName, columnName);
        }
        // 返回对象
        fromCursor.addStatement("return obj").returns(TypeName.OBJECT);
        return fromCursor.build();
    }

    /**
     * 将对象转换成 ContentValues的
     */
    public static MethodSpec toContentValues(TypeElement element)
    {
        String type = element.asType().toString();
        List<VariableElement> fields = ElementUtil.getFields(element);

        MethodSpec.Builder builder = MethodSpec.methodBuilder("toContentValues")
                .returns(Global.ContentValues)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassName.OBJECT, "obj");
        // 判断数据类型
        builder.addCode("if (!(obj instanceof $N))", type);
        // 抛出数据类型异常
        builder.addStatement("throw new RuntimeException(\"obj not instanceof $N\")", type);
        // 类型强转换
        builder.addStatement("$N new_obj = ($N)obj", type, type);
        // 新建对象
        builder.addStatement("$T values = new $T()", Global.ContentValues, Global.ContentValues);

        for (VariableElement field : fields)
        {
            // 变量名称
            String fieldName = field.getSimpleName().toString();
            // 字段名称
            String columnName = ElementUtil.getColumnName(field);
            // 转换器名称
            String convertFieldName = fieldName + Suffix.convert_suffix;
            // 拿到值的名称
            String valueName = fieldName + "_result";
            // 判断是否主键，并且是否是自动增长，如果是自动增长的数据，则不进行 增 改 的计算
            PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
            if (primaryKey != null && primaryKey.autoGenerate())
            {
                continue;
            }
            // 首先利用转换器，拿到转换后的值
            builder.addStatement("Object $N = $N.convertToDataBaseObject(new_obj.$N)", valueName, convertFieldName, fieldName);
            // 分别赋值
            builder.addStatement("this.setContentValue($N,$S,$N,values)", convertFieldName, columnName, valueName);
        }

        // 返回values对象
        builder.addStatement("return values");
        return builder.build();
    }

    /**
     * 根据传入的Convert类型，自动判断转对应的数据进行存入值
     */
    public static MethodSpec setContentValue()
    {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("setContentValue")
                .returns(ClassName.VOID)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Global.Convert, "convert")
                .addParameter(Global.String, "name")
                .addParameter(ClassName.OBJECT, "value")
                .addParameter(Global.ContentValues, "values");

        // ToByteConvert
        builder.addCode("if (convert instanceof $T)", Global.ToByteConvert);
        builder.addStatement("values.put(name, value == null ? null : ($T) value)", byte.class);
        // ToLongConvert
        builder.addCode("if (convert instanceof $T)", Global.ToLongConvert);
        builder.addStatement("values.put(name, value == null ? null : ($T) value)", long.class);
        // ToFloatConvert
        builder.addCode("if (convert instanceof $T)", Global.ToFloatConvert);
        builder.addStatement("values.put(name, value == null ? null : ($T) value)", float.class);
        // ToDoubleConvert
        builder.addCode("if (convert instanceof $T)", Global.ToDoubleConvert);
        builder.addStatement("values.put(name, value == null ? null : ($T) value)", double.class);
        // ToShortConvert
        builder.addCode("if (convert instanceof $T)", Global.ToShortConvert);
        builder.addStatement("values.put(name, value == null ? null : ($T) value)", short.class);
        // ToByteArrayConvert
        builder.addCode("if (convert instanceof $T)", Global.ToByteArrayConvert);
        builder.addStatement("values.put(name, value == null ? null : ($T) value)", byte[].class);
        // ToStringConvert
        builder.addCode("if (convert instanceof $T)", Global.ToStringConvert);
        builder.addStatement("values.put(name, value == null ? null : ($T) value)", String.class);
        // ToBooleanConvert
        builder.addCode("if (convert instanceof $T)", Global.ToBooleanConvert);
        builder.addStatement("values.put(name, value == null ? null : ($T) value)", boolean.class);
        // ToIntegerConvert
        builder.addCode("if (convert instanceof $T)", Global.ToIntegerConvert);
        builder.addStatement("values.put(name, value == null ? null : ($T) value)", int.class);

        return builder.build();
    }

    /**
     * 删除记录
     */
    public static MethodSpec delete(TypeElement element)
    {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("delete")
                .returns(int.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addException(Exception.class)
                .addParameter(Global.SQLiteWriter, "sqLite")
                .addParameter(Object.class, "obj");

        List<VariableElement> allFields = ElementUtil.getFields(element);
        List<VariableElement> keyFields = new LinkedList<>();
        for (VariableElement field : allFields)
        {
            if (ElementUtil.isPrimaryKey(field))
            {
                keyFields.add(field);
            }
        }
        String entityType = element.asType().toString();
        String tableName = ElementUtil.getTableName(element);

        builder.addStatement("if (obj == null) return 0");
        builder.addStatement("if (!(obj instanceof $N)) throw new Exception(obj + \" not instanceof $N\")", entityType, entityType);
        builder.addStatement("$N new_obj = ($N) obj", entityType, entityType);
        builder.addStatement("$T joiner = new StringJoiner(\" AND \")", StringJoiner.class);
        builder.addCode("for (String keyName : keyNames)");
        builder.addCode("{");
        builder.addStatement("joiner.add(keyName + \"=?\")");
        builder.addCode("}");
        builder.addStatement("$T[] values = new String[keyNames.length]", String.class);

        for (int i = 0; i < keyFields.size(); i++)
        {
            VariableElement keyField = keyFields.get(i);
            String fieldName = keyField.getSimpleName().toString();
            builder.addStatement("values[$L] = String.valueOf(new_obj.$N)", i, fieldName);
        }


        builder.addStatement("return sqLite.delete($S, $N, values)", tableName, "joiner.toString()");
        return builder.build();
    }

    /**
     * 更新记录
     */
    public static MethodSpec update(TypeElement element)
    {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("update")
                .returns(int.class)
                .addModifiers(Modifier.PUBLIC)
                .addException(Exception.class)
                .addAnnotation(Override.class)
                .addParameter(Global.SQLiteWriter, "sqLite")
                .addParameter(ConflictAlgorithm.class, "conflict")
                .addParameter(Object.class, "obj");

        List<VariableElement> allFields = ElementUtil.getFields(element);
        List<VariableElement> keyFields = new LinkedList<>();
        for (VariableElement field : allFields)
        {
            if (ElementUtil.isPrimaryKey(field))
            {
                keyFields.add(field);
            }
        }
        String entityType = element.asType().toString();
        String tableName = ElementUtil.getTableName(element);

        builder.addStatement("if (obj == null) return 0");
        builder.addStatement("if (!(obj instanceof $N)) throw new Exception(obj + \" not instanceof $N\")", entityType, entityType);
        builder.addStatement("$N new_obj = ($N) obj", entityType, entityType);
        builder.addStatement("$T joiner = new StringJoiner(\" AND \")", StringJoiner.class);
        builder.addCode("for (String keyName : keyNames)");
        builder.addCode("{");
        builder.addStatement("joiner.add(keyName + \"=?\")");
        builder.addCode("}");
        builder.addStatement("$T[] values = new String[keyNames.length]", String.class);

        for (int i = 0; i < keyFields.size(); i++)
        {
            VariableElement keyField = keyFields.get(i);
            String fieldName = keyField.getSimpleName().toString();
            builder.addStatement("values[$L] = String.valueOf(new_obj.$N)", i, fieldName);
        }

        builder.addStatement("ContentValues obj_values = this.toContentValues(obj)");
        builder.addStatement("sqLite.update($S,conflict.algorithm,obj_values,joiner.toString(),values)", tableName);
        builder.addStatement("return 1");
        return builder.build();
    }

    /**
     * 插入记录
     */
    public static MethodSpec insert(TypeElement element)
    {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("insert")
                .returns(int.class)
                .addModifiers(Modifier.PUBLIC)
                .addException(Exception.class)
                .addAnnotation(Override.class)
                .addParameter(Global.SQLiteWriter, "sqLite")
                .addParameter(ConflictAlgorithm.class, "conflict")
                .addParameter(Object.class, "obj");

        String entityType = element.asType().toString();
        String tableName = ElementUtil.getTableName(element);

        builder.addStatement("if (obj == null) return 0");
        builder.addStatement("if (!(obj instanceof $N)) throw new Exception(obj + \" not instanceof $N\")", entityType, entityType);

        builder.addStatement("ContentValues obj_values = this.toContentValues(obj)");
        builder.addStatement("sqLite.insert($S,conflict.algorithm,obj_values)", tableName);
        builder.addStatement("return 1");
        return builder.build();
    }
}
