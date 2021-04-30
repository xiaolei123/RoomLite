package me.xiaolei.roomlite.room_lite;

import me.xiaolei.room_lite.annotations.Entity;
import me.xiaolei.room_lite.annotations.PrimaryKey;

@Entity
public class User3
{
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
}
