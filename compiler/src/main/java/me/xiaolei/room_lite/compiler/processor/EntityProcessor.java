package me.xiaolei.room_lite.compiler.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

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
            Set<? extends Element> elements = environment.getElementsAnnotatedWith(Entity.class);
            for (Element element : elements)
            {
                this.compiler((TypeElement) element);
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

    private void compiler(TypeElement element) throws Exception
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
                .addAnnotation(Global.Keep)
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
    }
}
