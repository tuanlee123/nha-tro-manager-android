package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.data.entity.Room;

import java.util.List;

@Dao
public interface RoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Room room);

    @Update
    void update(Room room);

    @Delete
    void delete(Room room);

    @Query("DELETE FROM rooms WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM rooms WHERE id = :id LIMIT 1")
    Room getRoomById(long id);

    @Query("SELECT * FROM rooms WHERE qrJoinToken = :token LIMIT 1")
    Room getRoomByToken(String token);

    @Query("SELECT * FROM rooms WHERE roomNumber = :roomNumber LIMIT 1")
    Room getRoomByNumber(String roomNumber);

    @Query("SELECT * FROM rooms ORDER BY roomNumber ASC")
    List<Room> getAllRooms();

    @Query("SELECT * FROM rooms WHERE status = :status ORDER BY roomNumber ASC")
    List<Room> getRoomsByStatus(String status);

    @Query("SELECT COUNT(*) FROM rooms")
    int countRooms();

    @Query("SELECT COUNT(*) FROM rooms WHERE status = :status")
    int countRoomsByStatus(String status);
}
