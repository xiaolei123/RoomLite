package me.xiaolei.roomlite.room_lite;


import androidx.lifecycle.LiveData;

import java.util.Date;
import java.util.List;

import me.xiaolei.room_lite.annotations.Limit;
import me.xiaolei.room_lite.annotations.dao.Dao;
import me.xiaolei.room_lite.annotations.dao.Delete;
import me.xiaolei.room_lite.annotations.dao.Insert;
import me.xiaolei.room_lite.annotations.dao.Query;
import me.xiaolei.room_lite.annotations.dao.Update;

@Dao
public interface UserDao
{

    @Insert
    int addUser(User user);

    @Insert
    int addUser(User[] user);

    @Insert
    void addUser(List<User> user);

    @Insert
    void addUser(List<User> list, User[] array, User single);


    @Delete
    int delete(User user);


    @Delete
    int delete(User[] user);

    @Delete
    int delete(List<User> list, User[] array, User single);


    @Update
    int update(User single);

    @Update
    int update(User[] array);

    @Update
    int update(List<User> list);

    @Update
    int update(User single, User[] array, List<User> list);


    @Query(entity = User.class)
    User querySingle();

    @Query(entity = User.class, limit = @Limit(index = "0", maxLength = "1"))
    User querySingle1();

    @Query(entity = User.class, whereClause = "name like ?")
    User querySingle2(String like);

    @Query(entity = User.class)
    User[] queryArray();

    @Query(entity = User.class, limit = @Limit(index = "0", maxLength = "3"))
    User[] queryArray1();

    @Query(entity = User.class, whereClause = "name like ?")
    User[] queryArray2(String like);

    @Query(what = "count(*)", entity = User.class)
    int count();

    @Query(what = "name", entity = User.class, limit = @Limit(index = "0", maxLength = "10"))
    String[] names();

    @Query(what = "name", entity = User.class, limit = @Limit(index = "0", maxLength = "10"))
    List<String> names2();

    @Query(what = "date", entity = User.class)
    List<Date> dates();

    @Query(entity = User.class)
    LiveData<List<User>> asyncAll();

    @Query(what = "count(*)", entity = User.class)
    LiveData<Integer> asyncCount();

    @Query(entity = User.class)
    LiveData<List<Integer>> asyncAll2();

    @Query(entity = User.class)
    LiveData<String> asyncAll3();

    @Query(entity = User.class)
    LiveData<String[]> asyncAll4();

    @Query(entity = User.class)
    LiveData<Byte> asyncAll5();

    @Query(entity = User.class)
    LiveData<byte[]> asyncAll6();

    @Query(entity = User.class)
    LiveData<Byte[]> asyncAll7();
}
