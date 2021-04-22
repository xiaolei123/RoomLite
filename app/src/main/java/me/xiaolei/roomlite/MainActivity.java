package me.xiaolei.roomlite;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.util.Arrays;

import me.xiaolei.room_lite.runtime.RoomLite;
import me.xiaolei.roomlite.room_lite.DateConvert;
import me.xiaolei.roomlite.room_lite.LiteDataBase;
import me.xiaolei.roomlite.room_lite.User;
import me.xiaolei.roomlite.room_lite.UserDao;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView text = findViewById(R.id.text);

        RoomLite.addConvert(new DateConvert());

        LiteDataBase dataBase = RoomLite.build(LiteDataBase.class);
        UserDao userDao = dataBase.getDao(UserDao.class);

        text.setOnClickListener(v ->
        {
            roomLite(userDao);
        });
    }

    private void roomLite(UserDao dao)
    {
        long old_time = System.currentTimeMillis();

        System.out.println(dao.dates());

        System.out.println("耗时:" + (System.currentTimeMillis() - old_time));
    }
}