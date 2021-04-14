package me.xiaolei.room_lite.compiler.utils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;


public class Logger
{
    private final Messager msg;
    private final String globalKey;

    public Logger(Messager messager, AbstractProcessor processor)
    {
        msg = messager;
        globalKey = processor.getClass().getSimpleName();
    }

    /**
     * Print info log.
     */
    public void info(String info)
    {
        msg.printMessage(Diagnostic.Kind.NOTE, globalKey + " -> " + info);
    }

    public void error(CharSequence error)
    {
        msg.printMessage(Diagnostic.Kind.ERROR, globalKey + " -> " + "遇到异常, [" + error + "]");
    }

    public void error(Throwable error)
    {
        if (null != error)
        {
            msg.printMessage(Diagnostic.Kind.ERROR, globalKey + " -> " + "遇到异常, [" + error.getMessage() + "]" + "\n" + formatStackTrace(error.getStackTrace()));
        }
    }

    public void warning(CharSequence warning)
    {
        msg.printMessage(Diagnostic.Kind.WARNING, globalKey + " -> " + warning);
    }

    private String formatStackTrace(StackTraceElement[] stackTrace)
    {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTrace)
        {
            sb.append("    at ").append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}