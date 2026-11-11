package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.data.entity.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    User getUserByPhone(String phone);

    @Query("SELECT * FROM users WHERE phone = :phone AND password = :password LIMIT 1")
    User login(String phone, String password);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserById(long id);

    @Query("SELECT * FROM users WHERE role = 'OWNER' LIMIT 1")
    User getFirstOwner();

    @Query("SELECT * FROM users WHERE roomId = :roomId")
    List<User> getTenantsByRoom(long roomId);

    @Query("SELECT * FROM users WHERE role = 'TENANT' AND (roomId IS NULL OR roomId <= 0)")
    List<User> getTenantsWithoutRoom();

    @Query("SELECT * FROM users WHERE role = 'TENANT'")
    List<User> getAllTenants();

    @Query("SELECT COUNT(*) FROM users")
    int countUsers();
}
