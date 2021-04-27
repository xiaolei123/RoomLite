package me.xiaolei.room_lite.compiler.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import me.xiaolei.room_lite.Suffix;
import me.xiaolei.room_lite.annotations.Entity;
import me.xiaolei.room_lite.compiler.Global;
import me.xiaolei.room_lite.compiler.base.BaseProcessor;
import me.xiaolei.room_lite.compiler.utils.EntityHelperUtils;

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
        if (set == null || set.isEmpty())
        {
            return false;
        }
        try
        {
            logger.info("process-start");

            TypeSpec.Builder helpersBuilder = TypeSpec.classBuilder("EntityHelpers")
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(Global.AutoGenera)
                    .addAnnotation(Global.Keep);
            // 定义字段
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(ParameterizedTypeName.get(Map.class, Class.class, Object.class), "helpers")
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL);
            CodeBlock.Builder fieldInitBuilder = CodeBlock.of("new $T<$T,$T>()", HashMap.class, Class.class, Object.class)
                    .toBuilder()
                    .add("{")
                    .add("{");
            // 获取所有的Entity
            Set<? extends Element> elements = environment.getElementsAnnotatedWith(Entity.class);
            for (Element element : elements)
            {
                TypeElement typeElement = (TypeElement) element;
                // entity的类全名称
                String entityQualifiedName = typeElement.getQualifiedName().toString();
                // 生成的类的全名称
                String helperQualifiedName = this.compiler(typeElement);
                // 生成代码自动关联映射
                fieldInitBuilder.addStatement("put($N.class,new $N())", entityQualifiedName, helperQualifiedName);
            }
            fieldInitBuilder.add("}").add("}");
            // 添加字段
            helpersBuilder.addField(fieldBuilder.initializer(fieldInitBuilder.build()).build());

            // 重写函数
            MethodSpec maps = MethodSpec
                    .methodBuilder("maps")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addStatement("return helpers")
                    .returns(ParameterizedTypeName.get(Map.class, Class.class, Object.class)).build();
            
            // 把字段放进类里
            helpersBuilder.addMethod(maps);

            TypeSpec helpers = helpersBuilder.build();
            JavaFile javaFile = JavaFile.builder("me.xiaolei.room_lite.runtime.auto_genera", helpers).build();
            javaFile.writeTo(filer);
        } catch (Exception e)
        {
            logger.error(e);
        } finally
        {
            logger.info("process-end");
        }
        return true;
    }

    private String compiler(TypeElement element) throws Exception
    {
        // 首先检查Entity的合法性
        EntityHelperUtils.checkEntityLegitimate(element);
        // 日志里打印所有的Entity记录
        logger.info(element.asType().toString());
        // 获取类所在的包名
        String packageName = elementUtil.getPackageOf(element).asType().toString();
        // 获取类名
        String klassName = element.getSimpleName().toString();
        // 辅助类名
        String helperKlassName = klassName + Suffix.helper_suffix;
        // 新建辅助类
        TypeSpec.Builder helperClass = TypeSpec.classBuilder(helperKlassName)
                .addModifiers(Modifier.PUBLIC)
                //.addAnnotation(Global.Keep)
                .addSuperinterface(Global.EntityHelper);
        // 构造函数
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        // 获取主键数组名称数组
        FieldSpec keyNames = EntityHelperUtils.keyNames(element);
        // 对字段进行解析，并且生成对应的解析器的字段
        FieldSpec[] fieldSpecs = EntityHelperUtils.convertAndConstructor(constructor, element);
        // 实现获取表名
        MethodSpec getTableName = EntityHelperUtils.getTableName(element);
        // 实现获取创建表语句
        MethodSpec getCreateSQL = EntityHelperUtils.getCreateSQL(element);
        // 创建索引
        MethodSpec getCreateIndexSQL = EntityHelperUtils.getCreateIndexSQL(element);
        // 获取所有索引的名称
        MethodSpec indexNames = EntityHelperUtils.indexNames(element);
        // 获取所有数据库字段的名称
        MethodSpec columnNames = EntityHelperUtils.columnNames(element);
        // 创建实例
        MethodSpec newInstance = EntityHelperUtils.fromCursor(element);
        // 对象转换成contentValues
        MethodSpec toContentValues = EntityHelperUtils.toContentValues(element);
        // 生成自动判断并且存入值
        MethodSpec setContentValue = EntityHelperUtils.setContentValue();
        // 删除记录
        MethodSpec delete = EntityHelperUtils.delete(element);
        // 更新记录
        MethodSpec update = EntityHelperUtils.update(element);
        // 插入记录
        MethodSpec insert = EntityHelperUtils.insert(element);


        // 把方法添加到类里
        helperClass.addField(keyNames);
        helperClass.addMethod(constructor.build());
        helperClass.addMethod(getTableName);
        helperClass.addMethod(getCreateSQL);
        helperClass.addMethod(getCreateIndexSQL);
        helperClass.addMethod(indexNames);
        helperClass.addMethod(columnNames);
        helperClass.addMethod(newInstance);
        helperClass.addMethod(toContentValues);
        helperClass.addMethod(setContentValue);
        helperClass.addMethod(delete);
        helperClass.addMethod(update);
        helperClass.addMethod(insert);
        for (FieldSpec fieldSpec : fieldSpecs)
        {
            helperClass.addField(fieldSpec);
        }
        JavaFile javaFile = JavaFile.builder(packageName, helperClass.build()).build();
        javaFile.writeTo(filer);
        return packageName + "." + helperKlassName;
    }
}
