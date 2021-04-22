package me.xiaolei.roomlite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Arrays;

import me.xiaolei.room_lite.runtime.RoomLite;
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

        LiteDataBase dataBase = RoomLite.build(LiteDataBase.class);
        UserDao userDao = dataBase.getDao(UserDao.class);

        text.setOnClickListener(v ->
        {
            for (int i = 0; i < 10; i++)
            {
                new Thread(() -> roomLite(userDao)).start();
            }
        });
        
        LoaderManager.getInstance(this).initLoader(1, null, new LoaderManager.LoaderCallbacks<Cursor>()
        {
            @NonNull
            @Override
            public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args)
            {
                CursorLoader loader = new CursorLoader(MainActivity.this);
                loader.setUri(Uri.parse(""));
                loader.setSelection("");
                loader.setSelectionArgs(new String[]{});
                return loader;
            }

            @Override
            public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data)
            {

            }

            @Override
            public void onLoaderReset(@NonNull Loader<Cursor> loader)
            {

            }
        });
    }

    private void roomLite(UserDao dao)
    {
        long old_time = System.currentTimeMillis();
        dao.addUser(new User[]{new User(), new User()});
        //dao.queryArray();
        System.out.println("耗时:" + (System.currentTimeMillis() - old_time));
    }
}