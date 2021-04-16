package me.xiaolei.room_lite.compiler.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

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
import me.xiaolei.room_lite.annotations.PrimaryKey;
import me.xiaolei.room_lite.compiler.Global;
import me.xiaolei.room_lite.compiler.base.BaseProcessor;
import me.xiaolei.room_lite.compiler.utils.ElementUtil;
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
                .addAnnotation(Global.Keep)
                .addSuperinterface(ClassName.get(EntityHelper.class));
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


        // 把方法添加到类里
        helperClass.addField(keyNames);
        helperClass.addMethod(constructor.build());
        helperClass.addMethod(getTableName);
        helperClass.addMethod(getCreateSQL);
        helperClass.addMethod(newInstance);
        helperClass.addMethod(toContentValues);
        helperClass.addMethod(setContentValue);
        helperClass.addMethod(delete);
        helperClass.addMethod(update);
        for (FieldSpec fieldSpec : fieldSpecs)
        {
            helperClass.addField(fieldSpec);
        }
        JavaFile javaFile = JavaFile.builder(packageName, helperClass.build()).build();
        javaFile.writeTo(filer);

        logger.info("packageName ->" + packageName);
        logger.info("klassName ->" + klassName);
    }
}
