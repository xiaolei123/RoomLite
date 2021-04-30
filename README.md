# RoomLite

#### 介绍


Android平台下，使用编译期注解生成Java文件，拒绝低性能。一个SQLite数据库ORM的船新版本。增删改查建表，排序正序倒序唯一性，表索引，多线程并发读写都不在话下。确定不了解一下？

更新支持查询LiveData自动更新,自动跟随界面生命周期销毁资源

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
java:
dependencies {
    annotationProcessor 'com.github.xiaolei123:compiler:+'
    implementation 'com.github.xiaolei123:runtime:+'
}

kotlin:
dependencies {
    kapt 'com.github.xiaolei123:compiler:+'
    implementation 'com.github.xiaolei123:runtime:+'
}


```

[![版本信息](https://www.jitpack.io/v/com.github.xiaolei123/RoomLite.svg)](https://www.jitpack.io/#xiaolei123/RoomLite)


#### 使用说明

##### 1. 创建数据库
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
    public UpgradeOptions[] upgradeOptions()
    {
        return new UpgradeOptions[]{
                // 版本1升级版本2，更新了表User
                UpgradeOptions.upgrade(1, 2).update(User.class),
                // 版本2升级版本3，新增了个表User3
                UpgradeOptions.upgrade(2, 3).addTable(User3.class)
        };
    }
    
    // 数据库版本
    @Override
    public int version()
    {
        return 1;
    }
}
```

##### 2. 在数据库里创建表

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
###### 2.1 声明字段为主键（并且自增长）,自增长只有在类型为数字类型的时候才会生效
```java
@PrimaryKey(autoGenerate = true)
```
###### 2.2 字段 NOT NULL
```java
@Column(notNull = true)
```

###### 2.3 字段唯一 UNIQUE

```java
@Column(unique = true)
```

###### 2.4 默认值 DEFAULT
```java
@Column( defaultValue = "0")
```

###### 2.5 忽略某个字段
```java
@Ignore
public Bitmap bitmap;
```

###### 2.6 支持自定义字段
 
第一步：在表类里声明自定义类型
```java
@Entity(name = "User")
public class User
{
    public Date date;
}
```
第二步：继承对应的转换器
```java
public class DateConvert extends ToLongConvert<Date>
{
    public DateConvert()
    {
        super(Date.class);
    }
    @Override
    public Long convertToLong(Date javaObj)
    {
        Date date = (Date) javaObj;
        if (javaObj == null) 
            return null;
        return date.getTime();
    }
    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Date cursorToJavaObject(long value)
    {
        return new Date(value);
    }
}
```
第三步：向RoomLite注册转换器
```java
RoomLite.addConvert(new DateConvert());
```

##### 3. 创建索引 方式一

```java
@Entity(name = "User", indices = {
        @Index(columnNames = {"id", "name"}),
        @Index(name = "index2", columnNames = {"id", "name"}),
        @Index(name = "index3", columnNames = {"id", "name"}, unique = true),
})
```

##### 4. 创建索引 方式二

```java
@Column(index = true)
```



##### 5. 创建Dao

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

##### 6. 获取DataBase实例,获得Dao

```java
DataBase dataBase = RoomLite.build(DataBase.class);
UserDao dao = dataBase.getDao(UserDao.class);
```

##### 7. 增删改查使用

###### 7.1 增

```java
@Insert
public int addUser(User user);
@Insert
public void addUser(User[] users);
@Insert
public void addUserList(List<User> users);
```

###### 7.2 删除
```java
@Delete
public int deleteUser(User user);
@Delete
public void deleteUser(User[] users);
@Delete
public void deleteUserList(List<User> users);
```

###### 7.3 改
```java
@Update
public int updateUser(User user);
@Update
public void updateUser(User[] users);
@Update
public void updateUserList(List<User> users);
```

###### 7.4 查
```java
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
```

###### 7.5 查询分页
```java
@Query(entity = User.class, whereClause = "name like ?",limit=@Limit(index = "0", maxLength = "30"))
```

###### 7.6 查询占位符
```java
@Query(entity = User.class, whereClause = "name like ?",limit=@Limit(index = "0", maxLength = "?"))
```

###### 7.7 查询排序-正序
```java
@Query(entity = User.class, orderBy = @OrderBy(columnNames = {"id"}, type = OrderBy.Type.ASC))
```

###### 7.8 查询排序-倒序
```java
@Query(entity = User.class, orderBy = @OrderBy(columnNames = {"id"}, type = OrderBy.Type.DESC))
```

###### 7.9 查询LiveData支持
```java
@Query(what = "name", entity = User.class)
public LiveData<String[]> queryNames();
```

> End.