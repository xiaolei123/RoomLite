package me.xiaolei.roomlite.room_lite;

import me.xiaolei.room_lite.annotations.Entity;
import me.xiaolei.room_lite.annotations.PrimaryKey;

@Entity
public class People
{
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public String gender;
}
