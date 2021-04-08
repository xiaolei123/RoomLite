package me.xiaolei.roomlite;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.util.Arrays;

import me.xiaolei.myroom.library.RoomLite;
import me.xiaolei.roomlite.room.PeopleDao;
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

        RoomLite.addConvert(DateConvert.class);

        LiteDataBase dataBase = RoomLite.build(LiteDataBase.class);
        UserDao userDao = dataBase.getDao(UserDao.class);

        // RoomDataBase db = Room.databaseBuilder(getApplicationContext(), RoomDataBase.class, "peoples")
        //         .allowMainThreadQueries()
        //         .build();
        // PeopleDao peopleDao = db.peopleDao();

        text.setOnClickListener(v ->
        {
            for (int i = 0; i < 10; i++)
            {
                new Thread()
                {
                    @Override
                    public void run()
                    {
                        roomLite(userDao);
                    }
                }.start();
            }
            //room(peopleDao);
        });
    }

    private void roomLite(UserDao dao)
    {
        long old_time = System.currentTimeMillis();
        dao.addUser(new User[]{new User(), new User(), new User()});
        dao.getFirst();
        dao.queryAll();
        dao.query();
        dao.queryCount();
        dao.firstId();
        dao.queryNames();
        System.out.println("耗时：" + (System.currentTimeMillis() - old_time));
    }

    private void room(PeopleDao dao)
    {
        long old_time = System.currentTimeMillis();
        int id = dao.firstId();
        System.out.println("Room-耗时:" + (System.currentTimeMillis() - old_time));
        //System.out.println(id);
    }
}