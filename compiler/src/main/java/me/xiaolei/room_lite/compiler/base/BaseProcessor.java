package me.xiaolei.room_lite.compiler.base;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import me.xiaolei.room_lite.compiler.utils.Logger;

public abstract class BaseProcessor extends AbstractProcessor
{
    private final Set<String> annotationSet = new HashSet<>();
    protected Logger logger;
    protected Elements elementUtil;
    protected Filer filer;
    protected Types typeUtil;

    @Override
    public synchronized void init(ProcessingEnvironment environment)
    {
        super.init(environment);
        filer = environment.getFiler();
        logger = new Logger(environment.getMessager(), this);
        elementUtil = environment.getElementUtils();
        typeUtil = environment.getTypeUtils();
    }

    public BaseProcessor(Class<?>[] klass)
    {
        for (Class<?> aClass : klass)
        {
            annotationSet.add(aClass.getCanonicalName());
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        return annotationSet;
    }
}
