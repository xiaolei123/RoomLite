package me.xiaolei.roomlite;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import me.xiaolei.room_lite.runtime.RoomLite;
import me.xiaolei.room_lite.runtime.adapters.Impls.LiveDataAdapter;
import me.xiaolei.roomlite.room_lite.DateConvert;
import me.xiaolei.roomlite.room_lite.LiteDataBase;
import me.xiaolei.roomlite.room_lite.People;
import me.xiaolei.roomlite.room_lite.PeopleDao;
import me.xiaolei.roomlite.room_lite.User;
import me.xiaolei.roomlite.room_lite.UserDao;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView people_count = findViewById(R.id.people_count);
        Button add_people = findViewById(R.id.add_people);
        Button delete_people = findViewById(R.id.delete_people);

        TextView user_count = findViewById(R.id.user_count);
        Button add_user = findViewById(R.id.add_user);
        Button delete_user = findViewById(R.id.delete_user);


        RoomLite.addConvert(new DateConvert());

        LiteDataBase dataBase = RoomLite.build(LiteDataBase.class);
        UserDao userDao = dataBase.getDao(UserDao.class);
        PeopleDao peopleDao = dataBase.getDao(PeopleDao.class);


        peopleDao.syncCount().observe(this, count ->
        {
            people_count.setText("People总数:" + count);
        });
        userDao.asyncCount().observe(this, count ->
        {
            user_count.setText("User总数:" + count);
        });

        add_people.setOnClickListener(v ->
        {
            peopleDao.addPeople(new People());
        });
        delete_people.setOnClickListener(v ->
        {
            People people = peopleDao.first();
            peopleDao.delete(people);
        });

        add_user.setOnClickListener(v ->
        {
            userDao.addUser(new User());
        });
        delete_user.setOnClickListener(v ->
        {
            User user = userDao.querySingle1();
            userDao.delete(user);
        });
    }
}