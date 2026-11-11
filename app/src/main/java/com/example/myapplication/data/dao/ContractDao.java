package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.data.entity.Contract;

import java.util.List;

@Dao
public interface ContractDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Contract contract);

    @Update
    void update(Contract contract);

    @Delete
    void delete(Contract contract);

    @Query("SELECT * FROM contracts WHERE id = :id LIMIT 1")
    Contract getContractById(long id);

    @Query("SELECT * FROM contracts WHERE roomId = :roomId AND isActive = 1 LIMIT 1")
    Contract getActiveContractByRoom(long roomId);

    @Query("SELECT * FROM contracts WHERE tenantId = :tenantId AND isActive = 1 LIMIT 1")
    Contract getActiveContractByTenant(long tenantId);

    @Query("SELECT * FROM contracts WHERE isActive = 1")
    List<Contract> getAllActiveContracts();

    @Query("SELECT * FROM contracts WHERE roomId = :roomId")
    List<Contract> getContractsByRoom(long roomId);

    @Query("DELETE FROM contracts WHERE roomId = :roomId")
    void deleteByRoomId(long roomId);
}
