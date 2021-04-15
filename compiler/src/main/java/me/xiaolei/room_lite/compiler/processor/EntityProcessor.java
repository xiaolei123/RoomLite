package me.xiaolei.room_lite.compiler.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import me.xiaolei.room_lite.EntityHelper;
import me.xiaolei.room_lite.annotations.Column;
import me.xiaolei.room_lite.annotations.Entity;
import me.xiaolei.room_lite.annotations.Ignore;
import me.xiaolei.room_lite.compiler.Global;
import me.xiaolei.room_lite.compiler.base.BaseProcessor;
import me.xiaolei.room_lite.compiler.utils.ElementUtil;

/**
 * 用来解析Entity注解的类，并且生成对应的辅助类方便快速执行，增加runtime执行速度
 */
@AutoService(Processor.class)
public class EntityProcessor extends BaseProcessor
{
    public EntityProcessor()
    {
        super(new Class[]{Entity.class});
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment environment)
    {
        logger.info("process-start");
        try
        {
            if (set == null || set.isEmpty())
            {
                logger.info("annotations == null or empty");
                return false;
            }
            Set<? extends Element> elements = environment.getElementsAnnotatedWith(Entity.class);
            for (Element element : elements)
            {
                this.compiler(element);
            }
        } catch (Exception e)
        {
            logger.error(e);
        } finally
        {
            logger.info("process-end");
        }
        return true;
    }

    private void compiler(Element element) throws Exception
    {
        TypeMirror tm = element.asType();
        Entity entity = element.getAnnotation(Entity.class);
        logger.info(tm.toString() + "->" + entity);
        // 获取类所在的包名
        String packageName = elementUtil.getPackageOf(element).asType().toString();
        // 获取类名
        String klassName = element.getSimpleName().toString();
        // 辅助类名
        String helperKlassName = klassName + "$$EntityHelper";
        // 新建辅助类
        TypeSpec.Builder helperClass = TypeSpec.classBuilder(helperKlassName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(EntityHelper.class));
        // 构造函数
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        // 对字段进行解析，并且生成对应的解析器的字段
        FieldSpec[] fieldSpecs = this.convertAndConstructor(constructor, element);
        // 实现获取表名
        MethodSpec getTableName = this.getTableName(element);
        // 实现获取创建表语句
        MethodSpec getCreateSQL = this.getCreateSQL(element);
        // 创建实例
        MethodSpec newInstance = this.fromCursor(element);

        // 把方法添加到类里
        helperClass.addMethod(constructor.build());
        helperClass.addMethod(getTableName);
        helperClass.addMethod(getCreateSQL);
        helperClass.addMethod(newInstance);
        for (FieldSpec fieldSpec : fieldSpecs)
        {
            helperClass.addField(fieldSpec);
        }
        JavaFile javaFile = JavaFile.builder(packageName, helperClass.build()).build();
        javaFile.writeTo(filer);

        logger.info("packageName ->" + packageName);
        logger.info("klassName ->" + klassName);
    }


    /**
     * 对字段进行解析，并且生成对应的解析器的字段
     */
    private FieldSpec[] convertAndConstructor(MethodSpec.Builder constructor, Element element)
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
            String convertFieldName = fieldName + "$$convert";
            // 在构造函数里进行获取
            constructor.addStatement("this.$N = $T.getConvert($T.class)", convertFieldName, Global.Converts, field.asType());

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
    private MethodSpec getTableName(Element element)
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
    private MethodSpec getCreateSQL(Element element) throws Exception
    {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getCreateSQL")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(Global.String);
        // 获取所有字段
        List<VariableElement> fields = ElementUtil.getFields(element);
        // 获取表名
        String tableName = ElementUtil.getTableName(element);
        // 判断是否有主键
        if (!ElementUtil.hasPrimaryKey(fields))
            throw new Exception(element + " 必须含有至少一个主键 @PrimaryKey 使用");
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
                builder.addStatement("$T $N = Column.SQLType.valueOf($S)", Column.SQLType.class, sqlTypeName, column.type());
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
     * 创建生成对象的
     */
    private MethodSpec fromCursor(Element element)
    {
        TypeElement typeElement = ((TypeElement) element);
        // 获取所有的字段
        List<VariableElement> fields = ElementUtil.getFields(element);

        String klassName = typeElement.getQualifiedName().toString();
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
            String convertName = fieldName + "$$convert";
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
}
