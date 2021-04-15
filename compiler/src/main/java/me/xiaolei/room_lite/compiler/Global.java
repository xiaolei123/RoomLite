package me.xiaolei.room_lite.compiler;

import com.squareup.javapoet.ClassName;

public class Global
{
    // 转换器的类
    public static ClassName Convert = ClassName.get("me.xiaolei.room_lite.runtime.coverts", "Convert");
    // 数据库的游标类
    public static ClassName Cursor = ClassName.get("android.database", "Cursor");
    // 字符串类
    public static ClassName String = ClassName.get(java.lang.String.class);
    // 转换器的类集合操作类
    public static ClassName Converts = ClassName.get("me.xiaolei.room_lite.runtime.coverts", "Converts");
    // Keep注解
    public static ClassName Keep = ClassName.get("androidx.annotation", "Keep");
}
