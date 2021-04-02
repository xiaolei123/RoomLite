# RoomLite

#### 介绍
使用运行时反射实现的基于Android平台下的SQLite数据库ORM

#### 获取

```gradle
allprojects {
	repositories {
		...
		maven { url 'https://www.jitpack.io' }
	}
}
```

```gradle
dependencies {
	implementation 'com.github.xiaolei123:RoomLite:Tag'
}
```

![alt](https://www.jitpack.io/v/com.github.xiaolei123/RoomLite.svg)


#### 使用说明

1. 创建DataBase
```java
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
```

2. 创建Entity
```java
@Entity(name = "User")
public class User
{
    @Column
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name = "当前时间:" + System.currentTimeMillis();

    @Ignore
    public Bitmap bitmap;
}
```

3. 创建Dao

```java
@Dao
public interface UserDao
{
    @Insert
    public int addUser(User user);

    @Delete
    public int deleteUser(User user);

    @Update
    public void update(User user);

    @Query(entity = User.class, limit = "0,1")
    public User query();
}
```

4. 获取DataBase实例,获得Dao

```java
DataBase dataBase = RoomLite.build(DataBase.class);
UserDao dao = dataBase.getDao(UserDao.class);
```

 - 增

```java
@Dao
public interface UserDao
{
    @Insert
    public int addUser(User user);

    @Insert
    public void addUser(User[] users);
    
    @Insert
    public void addUserList(List<User> users);
}
```

 - 删除
```java
@Dao
public interface UserDao
{
    @Delete
    public int deleteUser(User user);

    @Delete
    public void deleteUser(User[] users);
    
    @Delete
    public void deleteUserList(List<User> users);
}
```

 - 改
```java
@Dao
public interface UserDao
{
    @Update
    public int updateUser(User user);

    @Update
    public void updateUser(User[] users);
    
    @Update
    public void updateUserList(List<User> users);
}
```

 - 查
```java
@Dao
public interface UserDao
{
    // 查询所有
    @Query(entity = User.class)
    public List<User> queryAll();

    // 查询第一个
    @Query(entity = User.class, limit = "0,1")
    public User query();

    // 查询总数
    @Query(what = "count(id)", entity = User.class)
    public int queryCount();

    // 查询所有的名字
    @Query(what = "name", entity = User.class)
    public String[] queryNames();

    // 模糊查询
    @Query(entity = User.class, whereClause = "name like ?")
    public User[] querySearch(String name);
}
```