package me.xiaolei.room_lite.compiler.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import me.xiaolei.room_lite.annotations.AutoRegister;
import me.xiaolei.room_lite.compiler.Global;
import me.xiaolei.room_lite.compiler.base.BaseProcessor;

/**
 * 自动注册
 */
@AutoService(Processor.class)
public class AutoRegisterProcessor extends BaseProcessor
{
    public AutoRegisterProcessor()
    {
        super(new Class[]{AutoRegister.class});
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment environment)
    {
        if (set == null || set.isEmpty())
        {
            return false;
        }

        logger.info("process-start");
        try
        {
            TypeElement autoRegisterElement = elementUtil.getTypeElement(Global.AutoRegister.canonicalName());
            List<? extends Element> methods = autoRegisterElement.getEnclosedElements();
            // register
            ExecutableElement registerElement = methods.stream()
                    .filter(element -> "register".equals(element.getSimpleName().toString()))
                    .map(element -> (ExecutableElement) element)
                    .findFirst()
                    .get();


            // 新建帮助类
            TypeSpec.Builder helpers = TypeSpec.classBuilder("AutoRegisterHelpers")
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(Global.AutoRegister)
                    .addAnnotation(Global.Keep);
            // 重写注册函数
            MethodSpec.Builder register = MethodSpec.overriding(registerElement);
            
            Set<? extends Element> elements = environment.getElementsAnnotatedWith(AutoRegister.class);
            for (Element element : elements)
            {
                TypeElement typeElement = (TypeElement) element;
                String qualifiedName = typeElement.getQualifiedName().toString();
                register.addStatement("this.regist($N.class)", qualifiedName);
            }

            helpers.addMethod(register.build());
            JavaFile javaFile = JavaFile.builder("me.xiaolei.room_lite.runtime.auto_genera", helpers.build()).build();
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
}
