package com.example.myapplication.ui.owner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.myapplication.R;
import com.example.myapplication.data.database.AppDatabase;
import com.example.myapplication.data.entity.Contract;
import com.example.myapplication.data.entity.Room;
import com.example.myapplication.data.entity.User;
import com.example.myapplication.ui.auth.LoginActivity;
import com.example.myapplication.utils.SessionManager;
import com.example.myapplication.worker.ContractReminderWorker;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class OwnerMainActivity extends AppCompatActivity implements RoomAdapter.OnRoomActionListener {

    private static final int REQ_NOTIFICATION_PERMISSION = 101;

    private TextView tvOwnerName;
    private TextView tvBoardingHouseName;
    private Button btnLogoutOwner;
    private Button btnCheckContractExpiry;

    private TextView tvTotalRoomsCount;
    private TextView tvVacantRoomsCount;
    private TextView tvOccupiedRoomsCount;
    private TextView tvDebtRoomsCount;

    private ChipGroup chipGroupFilter;
    private Chip chipAll;
    private Chip chipVacant;
    private Chip chipOccupied;
    private Chip chipDebt;

    private RecyclerView rvRooms;
    private TextView tvEmptyRooms;
    private ExtendedFloatingActionButton fabAddRoom;

    private AppDatabase db;
    private SessionManager sessionManager;
    private RoomAdapter adapter;

    private final List<RoomAdapter.RoomItem> allRoomItems = new ArrayList<>();
    private int currentFilterId = R.id.chipAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_main);

        db = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupRecyclerView();
        setupListeners();
        schedulePeriodicContractReminder();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOwnerAndRoomData();
    }

    private void initViews() {
        tvOwnerName = findViewById(R.id.tvOwnerName);
        tvBoardingHouseName = findViewById(R.id.tvBoardingHouseName);
        btnLogoutOwner = findViewById(R.id.btnLogoutOwner);
        btnCheckContractExpiry = findViewById(R.id.btnCheckContractExpiry);

        tvTotalRoomsCount = findViewById(R.id.tvTotalRoomsCount);
        tvVacantRoomsCount = findViewById(R.id.tvVacantRoomsCount);
        tvOccupiedRoomsCount = findViewById(R.id.tvOccupiedRoomsCount);
        tvDebtRoomsCount = findViewById(R.id.tvDebtRoomsCount);

        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        chipAll = findViewById(R.id.chipAll);
        chipVacant = findViewById(R.id.chipVacant);
        chipOccupied = findViewById(R.id.chipOccupied);
        chipDebt = findViewById(R.id.chipDebt);

        rvRooms = findViewById(R.id.rvRooms);
        tvEmptyRooms = findViewById(R.id.tvEmptyRooms);
        fabAddRoom = findViewById(R.id.fabAddRoom);

        tvOwnerName.setText("Xin chào, " + sessionManager.getFullName());
    }

    private void setupRecyclerView() {
        adapter = new RoomAdapter(this);
        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        rvRooms.setAdapter(adapter);
    }

    private void setupListeners() {
        btnLogoutOwner.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(OwnerMainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnCheckContractExpiry.setOnClickListener(v -> triggerContractReminderCheck());

        fabAddRoom.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerMainActivity.this, RoomFormActivity.class);
            startActivity(intent);
        });

        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                chipAll.setChecked(true);
                currentFilterId = R.id.chipAll;
            } else {
                currentFilterId = checkedId;
            }
            applyFilter();
        });
    }

    private void loadOwnerAndRoomData() {
        long ownerId = sessionManager.getUserId();
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            User owner = db.userDao().getUserById(ownerId);
            List<Room> rooms = db.roomDao().getAllRooms();
            List<Long> debtRoomIds = db.invoiceDao().getRoomIdsWithUnpaidInvoices();
            Set<Long> debtSet = new HashSet<>();
            if (debtRoomIds != null) {
                debtSet.addAll(debtRoomIds);
            }

            // Build map of room -> tenant name
            Map<Long, String> tenantNameMap = new HashMap<>();
            List<Contract> activeContracts = db.contractDao().getAllActiveContracts();
            if (activeContracts != null) {
                for (Contract c : activeContracts) {
                    User tenant = db.userDao().getUserById(c.getTenantId());
                    if (tenant != null && tenant.getFullName() != null) {
                        tenantNameMap.put(c.getRoomId(), tenant.getFullName());
                    }
                }
            }

            List<RoomAdapter.RoomItem> items = new ArrayList<>();
            int vacantCount = 0;
            int occupiedCount = 0;
            int debtCount = 0;

            if (rooms != null) {
                for (Room r : rooms) {
                    boolean hasDebt = debtSet.contains(r.getId());
                    if (hasDebt) {
                        debtCount++;
                    }
                    if ("DANG_THUE".equals(r.getStatus())) {
                        occupiedCount++;
                    } else {
                        vacantCount++;
                    }
                    String tenantName = tenantNameMap.get(r.getId());
                    items.add(new RoomAdapter.RoomItem(r, tenantName, hasDebt));
                }
            }

            final int finalTotal = rooms != null ? rooms.size() : 0;
            final int finalVacant = vacantCount;
            final int finalOccupied = occupiedCount;
            final int finalDebt = debtCount;

            runOnUiThread(() -> {
                if (owner != null && owner.getBoardingHouseName() != null) {
                    tvBoardingHouseName.setText("Khu trọ: " + owner.getBoardingHouseName());
                }

                tvTotalRoomsCount.setText(String.valueOf(finalTotal));
                tvVacantRoomsCount.setText(String.valueOf(finalVacant));
                tvOccupiedRoomsCount.setText(String.valueOf(finalOccupied));
                tvDebtRoomsCount.setText(String.valueOf(finalDebt));

                chipAll.setText("Tất cả (" + finalTotal + ")");
                chipVacant.setText("Phòng trống (" + finalVacant + ")");
                chipOccupied.setText("Đang thuê (" + finalOccupied + ")");
                chipDebt.setText("Còn nợ tiền (" + finalDebt + ")");

                allRoomItems.clear();
                allRoomItems.addAll(items);
                applyFilter();
            });
        });
    }

    private void applyFilter() {
        List<RoomAdapter.RoomItem> filtered = new ArrayList<>();
        for (RoomAdapter.RoomItem item : allRoomItems) {
            if (currentFilterId == R.id.chipVacant) {
                if (!"DANG_THUE".equals(item.room.getStatus())) {
                    filtered.add(item);
                }
            } else if (currentFilterId == R.id.chipOccupied) {
                if ("DANG_THUE".equals(item.room.getStatus())) {
                    filtered.add(item);
                }
            } else if (currentFilterId == R.id.chipDebt) {
                if (item.hasDebt) {
                    filtered.add(item);
                }
            } else {
                // All
                filtered.add(item);
            }
        }

        adapter.setData(filtered);
        if (filtered.isEmpty()) {
            tvEmptyRooms.setVisibility(View.VISIBLE);
            rvRooms.setVisibility(View.GONE);
        } else {
            tvEmptyRooms.setVisibility(View.GONE);
            rvRooms.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRoomClick(Room room) {
        Intent intent = new Intent(this, RoomDetailContractActivity.class);
        intent.putExtra(RoomDetailContractActivity.EXTRA_ROOM_ID, room.getId());
        startActivity(intent);
    }

    @Override
    public void onEditClick(Room room) {
        Intent intent = new Intent(this, RoomFormActivity.class);
        intent.putExtra(RoomFormActivity.EXTRA_ROOM_ID, room.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Room room) {
        boolean isOccupied = "DANG_THUE".equals(room.getStatus());
        String message = isOccupied
                ? "Phòng " + room.getRoomNumber() + " đang có người thuê. Xóa phòng sẽ hủy toàn bộ hợp đồng và hóa đơn liên quan. Bạn có chắc chắn muốn xóa không?"
                : "Bạn có chắc chắn muốn xóa phòng " + room.getRoomNumber() + " không?";

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa phòng")
                .setMessage(message)
                .setPositiveButton("Xóa phòng", (dialog, which) -> {
                    AppDatabase.getDatabaseWriteExecutor().execute(() -> {
                        // Unassign tenants
                        List<User> tenants = db.userDao().getTenantsByRoom(room.getId());
                        if (tenants != null) {
                            for (User t : tenants) {
                                t.setRoomId(null);
                                db.userDao().update(t);
                            }
                        }
                        // Delete contracts & invoices
                        db.contractDao().deleteByRoomId(room.getId());
                        db.invoiceDao().deleteByRoomId(room.getId());
                        // Delete room
                        db.roomDao().delete(room);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Đã xóa phòng " + room.getRoomNumber(), Toast.LENGTH_SHORT).show();
                            loadOwnerAndRoomData();
                        });
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void schedulePeriodicContractReminder() {
        PeriodicWorkRequest periodicRequest = new PeriodicWorkRequest.Builder(
                ContractReminderWorker.class,
                24,
                TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "ContractReminderWork",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
        );
    }

    private void triggerContractReminderCheck() {
        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIFICATION_PERMISSION
                );
                return;
            }
        }

        executeOneTimeContractCheck();
    }

    private void executeOneTimeContractCheck() {
        OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(ContractReminderWorker.class).build();
        WorkManager.getInstance(this).enqueue(oneTimeRequest);
        Toast.makeText(this, "Đang quét và kiểm tra hạn hợp đồng...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                executeOneTimeContractCheck();
            } else {
                Toast.makeText(this, "Cần cấp quyền thông báo để nhận cảnh báo hết hạn hợp đồng!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
