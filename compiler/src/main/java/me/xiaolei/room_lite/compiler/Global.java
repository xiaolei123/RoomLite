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
    // ContentValues
    public static ClassName ContentValues = ClassName.get("android.content", "ContentValues");
    
    public static ClassName ToByteConvert = ClassName.get("me.xiaolei.room_lite.runtime.coverts.base", "ToByteConvert");
    public static ClassName ToLongConvert = ClassName.get("me.xiaolei.room_lite.runtime.coverts.base", "ToLongConvert");
    public static ClassName ToFloatConvert = ClassName.get("me.xiaolei.room_lite.runtime.coverts.base", "ToFloatConvert");
    public static ClassName ToDoubleConvert = ClassName.get("me.xiaolei.room_lite.runtime.coverts.base", "ToDoubleConvert");
    public static ClassName ToShortConvert = ClassName.get("me.xiaolei.room_lite.runtime.coverts.base", "ToShortConvert");
    public static ClassName ToByteArrayConvert = ClassName.get("me.xiaolei.room_lite.runtime.coverts.base", "ToByteArrayConvert");
    public static ClassName ToStringConvert = ClassName.get("me.xiaolei.room_lite.runtime.coverts.base", "ToStringConvert");
    public static ClassName ToBooleanConvert = ClassName.get("me.xiaolei.room_lite.runtime.coverts.base", "ToBooleanConvert");
    public static ClassName ToIntegerConvert = ClassName.get("me.xiaolei.room_lite.runtime.coverts.base", "ToIntegerConvert");
    public static ClassName LiteDataBase = ClassName.get("me.xiaolei.room_lite.runtime.sqlite", "LiteDataBase");
    public static ClassName RoomLiteDatabase = ClassName.get("me.xiaolei.room_lite.runtime.sqlite", "RoomLiteDatabase");
    public static ClassName RoomLiteUtil = ClassName.get("me.xiaolei.room_lite.runtime.util", "RoomLiteUtil");
    public static ClassName Adapter = ClassName.get("me.xiaolei.room_lite.runtime.adapters", "Adapter");
    public static ClassName Adapters = ClassName.get("me.xiaolei.room_lite.runtime.adapters", "Adapters");
    public static ClassName Processor = ClassName.get("me.xiaolei.room_lite.runtime.adapters", "Processor");
    public static ClassName EntityHelper = ClassName.get("me.xiaolei.room_lite.runtime.entity", "EntityHelper");
    public static ClassName SQLiteReader = ClassName.get("me.xiaolei.room_lite.runtime.sqlite", "SQLiteReader");
    public static ClassName SQLiteWriter = ClassName.get("me.xiaolei.room_lite.runtime.sqlite", "SQLiteWriter");
    public static ClassName WriterRunnable = ClassName.get("me.xiaolei.room_lite.runtime.sqlite", "WriterRunnable");
}
