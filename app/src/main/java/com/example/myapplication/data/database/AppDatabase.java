package com.example.myapplication.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.myapplication.data.dao.ContractDao;
import com.example.myapplication.data.dao.InvoiceDao;
import com.example.myapplication.data.dao.RoomDao;
import com.example.myapplication.data.dao.UserDao;
import com.example.myapplication.data.entity.Contract;
import com.example.myapplication.data.entity.Invoice;
import com.example.myapplication.data.entity.Room;
import com.example.myapplication.data.entity.User;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Room.class, Contract.class, Invoice.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    public abstract UserDao userDao();
    public abstract RoomDao roomDao();
    public abstract ContractDao contractDao();
    public abstract InvoiceDao invoiceDao();

    public static ExecutorService getDatabaseWriteExecutor() {
        return databaseWriteExecutor;
    }

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = androidx.room.Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "motel_manager.db"
                            )
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    databaseWriteExecutor.execute(() -> seedInitialData(INSTANCE));
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void seedInitialData(AppDatabase db) {
        if (db == null) return;
        // Check if data already exists
        if (db.userDao().countUsers() == 0) {
            // 1. Create default Owner
            User owner = new User(
                    "Nguyễn Văn Chủ",
                    "0900000001",
                    "123456",
                    "OWNER",
                    null,
                    "Nhà Trọ Hạnh Phúc",
                    "970422", // MB Bank BIN
                    "0123456789",
                    "NGUYEN VAN CHU",
                    null
            );
            db.userDao().insert(owner);

            // Default Tenant 1 without room (to test JoinRoom flow)
            User tenant1 = new User(
                    "Trần Văn Thuê",
                    "0900000002",
                    "123456",
                    "TENANT",
                    "012345678901",
                    null,
                    null,
                    null,
                    null,
                    null
            );
            db.userDao().insert(tenant1);

            // Default Tenant 2 (staying in Room 102 with contract expiring in 7 days)
            User tenant2 = new User(
                    "Lê Thị Lan",
                    "0900000003",
                    "123456",
                    "TENANT",
                    "079199000123",
                    null,
                    null,
                    null,
                    null,
                    null
            );
            long tenant2Id = db.userDao().insert(tenant2);

            // 2. Create sample Room 101 (TRONG, has unpaid invoice)
            Room room101 = new Room(
                    "101",
                    2500000.0,
                    3500.0,
                    15000.0,
                    100000.0,
                    30000.0,
                    100000.0,
                    2,
                    "TRONG",
                    "JOIN101"
            );
            long room101Id = db.roomDao().insert(room101);

            // Sample Invoice for Room 101 (current month, unpaid)
            Calendar calendar = Calendar.getInstance();
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);

            int oldElec = 100;
            int newElec = 150;
            double elecTotal = (newElec - oldElec) * 3500.0; // 175,000

            int oldWater = 20;
            int newWater = 25;
            double waterTotal = (newWater - oldWater) * 15000.0; // 75,000

            int bikeCount = 2;
            double bikeTotal = bikeCount * 100000.0; // 200,000

            double serviceFee = 100000.0 + 30000.0; // 130,000 (wifi + garbage)
            double roomPrice = 2500000.0;
            double totalAmount = roomPrice + elecTotal + waterTotal + bikeTotal + serviceFee; // 3,080,000

            Invoice invoice101 = new Invoice(
                    room101Id,
                    month,
                    year,
                    oldElec,
                    newElec,
                    elecTotal,
                    oldWater,
                    newWater,
                    waterTotal,
                    bikeCount,
                    bikeTotal,
                    serviceFee,
                    roomPrice,
                    totalAmount,
                    false, // isPaid = false -> CÒN NỢ
                    0L,
                    System.currentTimeMillis()
            );
            db.invoiceDao().insert(invoice101);

            // 3. Create sample Room 102 (DANG_THUE, tenant2 staying, contract expires in 7 days)
            Room room102 = new Room(
                    "102",
                    3000000.0,
                    3500.0,
                    15000.0,
                    100000.0,
                    30000.0,
                    100000.0,
                    1,
                    "DANG_THUE",
                    "JOIN102"
            );
            long room102Id = db.roomDao().insert(room102);

            // Link tenant2 to Room 102
            tenant2.setId(tenant2Id);
            tenant2.setRoomId(room102Id);
            db.userDao().update(tenant2);

            // Create Contract for Room 102 ending in exactly 7 days
            long now = System.currentTimeMillis();
            long dayMs = 24L * 60 * 60 * 1000L;
            long startDate102 = now - 358L * dayMs; // ~1 year ago
            long endDate102 = now + 7L * dayMs;     // 7 days from now (triggers reminder!)
            Contract contract102 = new Contract(
                    room102Id,
                    tenant2Id,
                    startDate102,
                    endDate102,
                    3000000.0,
                    true // isActive
            );
            db.contractDao().insert(contract102);

            // 4. Create sample Room 103 (TRONG, no unpaid invoices)
            Room room103 = new Room(
                    "103",
                    2200000.0,
                    3500.0,
                    15000.0,
                    100000.0,
                    30000.0,
                    100000.0,
                    0,
                    "TRONG",
                    "JOIN103"
            );
            db.roomDao().insert(room103);
        }
    }
}
