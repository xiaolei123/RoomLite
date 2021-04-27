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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import me.xiaolei.room_lite.Suffix;
import me.xiaolei.room_lite.annotations.dao.Dao;
import me.xiaolei.room_lite.annotations.dao.Delete;
import me.xiaolei.room_lite.annotations.dao.Insert;
import me.xiaolei.room_lite.annotations.dao.Query;
import me.xiaolei.room_lite.annotations.dao.Update;
import me.xiaolei.room_lite.compiler.Global;
import me.xiaolei.room_lite.compiler.base.BaseProcessor;
import me.xiaolei.room_lite.compiler.utils.DaoProcessorUtil;

/**
 * 操作Dao
 */
@AutoService(Processor.class)
public class DaoProcessor extends BaseProcessor
{
    private DaoProcessorUtil processorUtil;

    public DaoProcessor()
    {
        super(new Class[]{Dao.class});
    }

    @Override
    public synchronized void init(ProcessingEnvironment environment)
    {
        super.init(environment);
        this.processorUtil = new DaoProcessorUtil(this.logger, this.elementUtil, this.filer, this.typeUtil);
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

            TypeSpec.Builder helpersBuilder = TypeSpec.classBuilder("DaoHelpers")
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

            Set<? extends Element> elements = environment.getElementsAnnotatedWith(Dao.class);
            for (Element element : elements)
            {
                TypeElement typeElement = (TypeElement) element;
                // entity的类全名称
                String entityQualifiedName = typeElement.getQualifiedName().toString();
                // 生成的类的全名称
                String helperQualifiedName = this.compiler(typeElement);
                // 生成代码自动关联映射
                fieldInitBuilder.addStatement("put($N.class,$N.class)", entityQualifiedName, helperQualifiedName);
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
        return false;
    }

    private String compiler(TypeElement element) throws Exception
    {
        // 日志里打印所有的Dao记录
        logger.info(element.asType().toString());

        if (!element.getKind().isInterface())
            throw new Exception("@" + Dao.class + " 必须作用于 interface 类上");
        // 获取类所在的包名
        String packageName = elementUtil.getPackageOf(element).asType().toString();
        // 获取类名
        String klassName = element.getSimpleName().toString();
        TypeName klass = ClassName.get(element.asType());
        // 辅助类名
        String daoImplKlassName = klassName + Suffix.dao_suffix;
        // 新建Dao的实现类
        TypeSpec.Builder implClass = TypeSpec.classBuilder(daoImplKlassName)
                .addModifiers(Modifier.PUBLIC)
                //.addAnnotation(Global.Keep)
                .addSuperinterface(klass);
        // 持有数据库的引用
        FieldSpec database = FieldSpec.builder(Global.RoomLiteDatabase, "database")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
        // 持有数据库的引用
        FieldSpec sqLite = FieldSpec.builder(Global.LiteDataBase, "sqLite")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
        implClass.addField(database);
        implClass.addField(sqLite);
        // 实现构造函数
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Global.RoomLiteDatabase, "database")
                .addParameter(Global.LiteDataBase, "sqLite")
                .addStatement("this.database = database")
                .addStatement("this.sqLite = sqLite")
                .build();
        implClass.addMethod(constructor);

        // 找出这个类中，所有的函数
        List<? extends Element> allElements = element.getEnclosedElements();
        for (Element m : allElements)
        {
            if (m instanceof ExecutableElement)
            {
                ExecutableElement method = (ExecutableElement) m;
                Insert insert = method.getAnnotation(Insert.class);
                Delete delete = method.getAnnotation(Delete.class);
                Update update = method.getAnnotation(Update.class);
                Query query = method.getAnnotation(Query.class);
                long count = Stream.of(insert, delete, update, query).filter(Objects::nonNull).count();
                if (count > 1)
                {
                    throw new Exception(method + " 不能使用多个注解在同一个函数上。");
                } else if (count == 0)
                {
                    throw new Exception(method + " 必须至少使用一个注解@Insert @Delete @Update @Query");
                }
                // 参数
                List<? extends VariableElement> params = method.getParameters();
                // 返回参数
                TypeMirror returnType = method.getReturnType();
                // 实现函数
                MethodSpec.Builder builder = MethodSpec.overriding(method);

                if (insert != null)
                {
                    processorUtil.insert(builder, method, insert, params, returnType);
                } else if (delete != null)
                {
                    processorUtil.delete(builder, method, delete, params, returnType);
                } else if (update != null)
                {
                    processorUtil.update(builder, method, update, params, returnType);
                } else if (query != null)
                {
                    processorUtil.query(implClass, builder, method, query, params, returnType);
                }
                implClass.addMethod(builder.build());
            }
        }

        JavaFile javaFile = JavaFile.builder(packageName, implClass.build()).build();
        javaFile.writeTo(filer);

        return packageName + "." + daoImplKlassName;
    }
}
