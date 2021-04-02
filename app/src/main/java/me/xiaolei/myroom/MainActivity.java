package me.xiaolei.myroom;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Query;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import me.xiaolei.myroom.library.sqlite.RoomLiteDatabase;
import me.xiaolei.myroom.library.RoomLite;
import me.xiaolei.myroom.roomlite.User;
import me.xiaolei.myroom.roomlite.UserDao;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataBase dataBase = RoomLite.build(DataBase.class);
        UserDao dao = dataBase.getDao(UserDao.class);

        TextView text = findViewById(R.id.text);
        text.setOnClickListener(v ->
        {
            long old_time = System.currentTimeMillis();
            
            User user = dao.query();
            System.out.println(System.currentTimeMillis() - old_time);
            System.out.println(user);
        });
    }

    public static class DataBase extends RoomLiteDatabase
    {
        public DataBase()
        {
            // 数据库名称
            super("school");
        }
        
        // 所有的表Entity
        @Override
        public Class<?>[] getEntities()
        {
            return new Class[]{User.class};
        }
        
        // 是否允许在主线程中执行
        @Override
        public boolean allowRunOnUIThread()
        {
            return true;
        }
        
        // 数据库升级
        @Override
        public void onUpgrade(@Nullable SQLiteDatabase db, int oldVersion, int newVersion)
        {
            
        }
        
        // 数据库版本
        @Override
        public int version()
        {
            return 1;
        }
    }
}