package me.xiaolei.roomlite;


import android.graphics.Bitmap;

import me.xiaolei.myroom.library.anno.Column;
import me.xiaolei.myroom.library.anno.Entity;
import me.xiaolei.myroom.library.anno.Ignore;
import me.xiaolei.myroom.library.anno.PrimaryKey;

@Entity(name = "User")
public class User
{
    @Column
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name = "当前时间:" + System.currentTimeMillis();

    @Ignore
    public Bitmap bitmap;
}
