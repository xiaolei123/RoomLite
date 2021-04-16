package me.xiaolei.roomlite;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import me.xiaolei.room_lite.runtime.RoomLite;
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

        RoomLite.addConvert(new DateConvert());

        LiteDataBase dataBase = RoomLite.build(LiteDataBase.class);
        UserDao userDao = dataBase.getDao(UserDao.class);

        // RoomDataBase db = Room.databaseBuilder(getApplicationContext(), RoomDataBase.class, "peoples")
        //         .allowMainThreadQueries()
        //         .build();
        // PeopleDao peopleDao = db.peopleDao();
        text.setOnClickListener(v ->
        {
            roomLite(userDao);
            //room(peopleDao);
        });
    }

    private void roomLite(UserDao dao)
    {
        long old_time = System.currentTimeMillis();
        long last_time = old_time;
        dao.addUser(new User[]{new User(), new User(), new User()});
        System.out.println("耗时：addUser-" + (System.currentTimeMillis() - last_time) + "->" + ((last_time = System.currentTimeMillis()) != 0));
        dao.getFirst();
        System.out.println("耗时：getFirst-" + (System.currentTimeMillis() - last_time) + "->" + ((last_time = System.currentTimeMillis()) != 0));
        dao.queryAll();
        System.out.println("耗时：queryAll-" + (System.currentTimeMillis() - last_time) + "->" + ((last_time = System.currentTimeMillis()) != 0));
        
        System.out.println("耗时：query-" + (System.currentTimeMillis() - last_time) + "->" + ((last_time = System.currentTimeMillis()) != 0));
        dao.queryCount();
        System.out.println("耗时：queryCount-" + (System.currentTimeMillis() - last_time) + "->" + ((last_time = System.currentTimeMillis()) != 0));
        dao.firstId();
        System.out.println("耗时：firstId-" + (System.currentTimeMillis() - last_time) + "->" + ((last_time = System.currentTimeMillis()) != 0));
        dao.queryNames();
        System.out.println("耗时：queryNames-" + (System.currentTimeMillis() - last_time) + "->" + ((last_time = System.currentTimeMillis()) != 0));

        System.out.println("总耗时：" + (System.currentTimeMillis() - old_time));
    }

    private void room(PeopleDao dao)
    {
        long old_time = System.currentTimeMillis();
        int id = dao.firstId();
        System.out.println("Room-耗时:" + (System.currentTimeMillis() - old_time));
        //System.out.println(id);
    }
}