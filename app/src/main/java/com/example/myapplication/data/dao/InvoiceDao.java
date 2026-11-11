package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.data.entity.Invoice;

import java.util.List;

@Dao
public interface InvoiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Invoice invoice);

    @Update
    void update(Invoice invoice);

    @Delete
    void delete(Invoice invoice);

    @Query("SELECT * FROM invoices WHERE roomId = :roomId ORDER BY year DESC, month DESC, id DESC LIMIT 1")
    Invoice getLatestInvoiceByRoom(long roomId);

    @Query("SELECT * FROM invoices WHERE roomId = :roomId AND isPaid = 0 LIMIT 1")
    Invoice getUnpaidInvoiceByRoom(long roomId);

    @Query("SELECT DISTINCT roomId FROM invoices WHERE isPaid = 0")
    List<Long> getRoomIdsWithUnpaidInvoices();

    @Query("SELECT * FROM invoices WHERE roomId = :roomId ORDER BY year DESC, month DESC, id DESC")
    List<Invoice> getInvoicesByRoom(long roomId);

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    Invoice getInvoiceById(long id);

    @Query("SELECT COUNT(*) FROM invoices")
    int countInvoices();

    @Query("DELETE FROM invoices WHERE roomId = :roomId")
    void deleteByRoomId(long roomId);
}
