package me.xiaolei.room_lite.compiler.processor;

import android.database.Cursor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import me.xiaolei.room_lite.EntityHelper;
import me.xiaolei.room_lite.annotations.Entity;
import me.xiaolei.room_lite.compiler.base.BaseProcessor;

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

        // 新建辅助类
        TypeSpec.Builder helperClass = TypeSpec.classBuilder(klassName + "$$EntityHelper")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(EntityHelper.class));
        // 实现获取表名
        MethodSpec getTableName = this.getTableName(element);

        // 实现获取创建表语句
        MethodSpec getCreateSQL = this.getCreateSQL(element);
        // 创建实例
        MethodSpec newInstance = this.newInstance(element);

        // 把方法添加到类里
        helperClass.addMethod(getTableName);
        helperClass.addMethod(getCreateSQL);
        helperClass.addMethod(newInstance);
        JavaFile javaFile = JavaFile.builder(packageName, helperClass.build()).build();
        javaFile.writeTo(filer);

        logger.info("packageName ->" + packageName);
        logger.info("klassName ->" + klassName);
    }

    /**
     * 解析表名的方法
     *
     * @param element
     * @return
     */
    private MethodSpec getTableName(Element element)
    {
        String klassName = element.getSimpleName().toString();
        Entity entity = element.getAnnotation(Entity.class);
        String tableName = entity.name();
        if (tableName.isEmpty())
        {
            tableName = klassName;
        }
        return MethodSpec.methodBuilder("getTableName")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("return $S", tableName)
                .returns(ClassName.get(String.class))
                .build();
    }

    /**
     * 解析生成SQL语句的方法
     *
     * @param element
     * @return
     */
    private MethodSpec getCreateSQL(Element element)
    {
        return MethodSpec.methodBuilder("getCreateSQL")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("return $S", "")
                .returns(ClassName.get(String.class))
                .build();
    }

    /**
     * 创建生成对象的
     */
    private MethodSpec newInstance(Element element)
    {
        String klassName = element.toString();
        return MethodSpec.methodBuilder("newInstance")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassName.get("android.database", "Cursor"), "cursor")
                .addStatement(klassName + " obj = new " + klassName + "()")
                .addStatement("return obj")
                .returns(TypeName.OBJECT)
                .build();
    }
}
