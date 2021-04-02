package me.xiaolei.roomlite;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.Bundle;
import android.widget.TextView;

import me.xiaolei.myroom.library.RoomLite;
import me.xiaolei.roomlite.room.PeopleDao;
import me.xiaolei.roomlite.room.RoomDataBase;
import me.xiaolei.roomlite.room_lite.LiteDataBase;
import me.xiaolei.roomlite.room_lite.UserDao;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView text = findViewById(R.id.text);

        LiteDataBase dataBase = RoomLite.build(LiteDataBase.class);
        UserDao userDao = dataBase.getDao(UserDao.class);
        RoomDataBase db = Room.databaseBuilder(getApplicationContext(), RoomDataBase.class, "peoples")
                .allowMainThreadQueries()
                .build();
        PeopleDao peopleDao = db.peopleDao();
        
        text.setOnClickListener(v ->
        {
            roomLite(userDao);
            room(peopleDao);
        });
    }

    private void roomLite(UserDao dao)
    {
        System.out.println("---------");
        
        long old_time = System.currentTimeMillis();
        int user = dao.firstId();
        System.out.println("RoomLite-耗时:" + (System.currentTimeMillis() - old_time));
        //System.out.println(user);
    }

    private void room(PeopleDao dao)
    {
        long old_time = System.currentTimeMillis();
        int id = dao.firstId();
        System.out.println("Room-耗时:" + (System.currentTimeMillis() - old_time));
        //System.out.println(id);
    }
}