package me.xiaolei.roomlite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import me.xiaolei.room_lite.runtime.RoomLite;
import me.xiaolei.room_lite.runtime.adapters.Impls.LiveDataAdapter;
import me.xiaolei.room_lite.runtime.sqlite.DataBaseProvider;
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
        RoomLite.addAdapter(new LiveDataAdapter());

        LiteDataBase dataBase = RoomLite.build(LiteDataBase.class);
        UserDao userDao = dataBase.getDao(UserDao.class);


        // LiveData<Integer> liveData = userDao.asyncCount();
        text.setOnClickListener(v ->
        {
            roomLite(userDao);
        });
        // liveData.observe(this, count ->
        // {
        //     Log.e("XIAOLEI", "自动更新总数:" + count);
        // });
    }

    private void roomLite(UserDao dao)
    {
        long old_time = System.currentTimeMillis();
        User user = dao.querySingle1();
        dao.delete(user);
        Log.e("XIAOLEI", "删除第一个");
    }
}