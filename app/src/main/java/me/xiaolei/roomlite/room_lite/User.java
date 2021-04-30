package me.xiaolei.roomlite.room_lite;

import java.util.Arrays;
import java.util.Date;

import me.xiaolei.room_lite.annotations.Column;
import me.xiaolei.room_lite.annotations.Entity;
import me.xiaolei.room_lite.annotations.Index;
import me.xiaolei.room_lite.annotations.PrimaryKey;

@Entity( indices = {
        @Index(columnNames = {"id", "name"}),
        @Index(name = "index2", columnNames = {"id", "name"}),
        @Index(name = "index3", columnNames = {"id", "name"}, unique = true),
})
public class User
{

    @Column(type = Column.SQLType.INTEGER, notNull = true, unique = true, defaultValue = "0", index = true)
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name = "当前时间:" + System.currentTimeMillis();

    @Column(name = "x_short")
    public short x_short;
    public byte x_byte;
    public double x_double;
    //public float x_float;
    //public long x_long;
    //public boolean x_bool;
    //public char x_char;
    //public Byte[] x_byte_array;
    //public Date date;

    @Override
    public String toString()
    {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", x_short=" + x_short +
                ", x_byte=" + x_byte +
                ", x_double=" + x_double +
                //", x_float=" + x_float +
                //", x_long=" + x_long +
                //", x_bool=" + x_bool +
                //", x_char=" + x_char +
                //", x_byte_array=" + Arrays.toString(x_byte_array) +
                //", date=" + date +
                '}';
    }
}
