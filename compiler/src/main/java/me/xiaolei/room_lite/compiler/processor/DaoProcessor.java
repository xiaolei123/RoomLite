package me.xiaolei.room_lite.compiler.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;
import java.util.Objects;
import java.util.Set;
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
            Set<? extends Element> elements = environment.getElementsAnnotatedWith(Dao.class);
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
        return false;
    }

    private void compiler(TypeElement element) throws Exception
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
        String daoImplKlassName = klassName + "$$DaoImpl";
        // 新建Dao的实现类
        TypeSpec.Builder implClass = TypeSpec.classBuilder(daoImplKlassName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Global.Keep)
                .addSuperinterface(klass);
        // 持有数据库的引用
        FieldSpec sqLite = FieldSpec.builder(Global.LiteDataBase, "sqLite")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
        implClass.addField(sqLite);
        // 实现构造函数
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Global.LiteDataBase, "sqLite")
                .addStatement("this.sqLite = sqLite")
                .build();
        implClass.addMethod(constructor);

        // 找出这个类中，所有的函数
        List<? extends Element> allElements = element.getEnclosedElements();
        for (Element m : allElements)
        {
            if (m instanceof ExecutableElement)
            {
                logger.info(m.getSimpleName().toString());
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
                    processorUtil.query(builder, method, query, params, returnType);
                }

                implClass.addMethod(builder.build());
            }
        }

        JavaFile javaFile = JavaFile.builder(packageName, implClass.build()).build();
        javaFile.writeTo(filer);
    }
}
