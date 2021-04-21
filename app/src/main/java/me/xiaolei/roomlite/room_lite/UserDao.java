package me.xiaolei.roomlite.room_lite;


import java.util.Date;
import java.util.List;

import me.xiaolei.room_lite.annotations.Limit;
import me.xiaolei.room_lite.annotations.OrderBy;
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


    @Query(what = "date", entity = User.class, limit = @Limit(index = "0", maxLength = "1"))
    public Date getFirst();

    // 查询所有
    @Query(entity = User.class)
    public List<User> queryAll();

    // 查询第一个
    @Query(entity = User.class, limit = @Limit(index = "0", maxLength = "1"))
    public User query();

    // 查询总数
    @Query(what = "count(id)", entity = User.class)
    public int queryCount();

    @Query(what = "id", entity = User.class, limit = @Limit(index = "0", maxLength = "1"))
    public int firstId();

    // 查询所有的名字
    @Query(what = "name", entity = User.class)
    public String[] queryNames();

    // 模糊查询
    @Query(entity = User.class, whereClause = "name like ?")
    public User[] querySearch(String name);

    // 模糊查询
    @Query(entity = User.class)
    public Date[] querySearch();

    @Query(what = "count(*)", entity = User.class)
    public Integer count();

    @Query(what = "id", entity = User.class, whereClause = "name=?", limit = @Limit(index = "0", maxLength = "30"), groupBy = {"id", "name"}, orderBy = @OrderBy(columnNames = {"id"}, type = OrderBy.Type.DESC))
    public User[] users();
}
