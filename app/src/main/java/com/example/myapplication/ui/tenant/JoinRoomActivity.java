package com.example.myapplication.ui.tenant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.database.AppDatabase;
import com.example.myapplication.data.entity.Contract;
import com.example.myapplication.data.entity.Room;
import com.example.myapplication.data.entity.User;
import com.example.myapplication.ui.auth.LoginActivity;
import com.example.myapplication.utils.SessionManager;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class JoinRoomActivity extends AppCompatActivity {

    private TextView tvTenantName;
    private Button btnLogout;
    private Button btnScanQr;
    private EditText etRoomToken;
    private Button btnConfirmJoin;

    private AppDatabase db;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<ScanOptions> qrScannerLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String scannedToken = result.getContents().trim();
                    handleJoinWithToken(scannedToken);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);

        db = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvTenantName = findViewById(R.id.tvTenantName);
        btnLogout = findViewById(R.id.btnLogout);
        btnScanQr = findViewById(R.id.btnScanQr);
        etRoomToken = findViewById(R.id.etRoomToken);
        btnConfirmJoin = findViewById(R.id.btnConfirmJoin);

        if (!TextUtils.isEmpty(sessionManager.getFullName())) {
            tvTenantName.setText("Xin chào, " + sessionManager.getFullName());
        }
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(JoinRoomActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnScanQr.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Hướng máy ảnh về phía mã QR phòng trọ");
            options.setBeepEnabled(true);
            options.setOrientationLocked(false);
            qrScannerLauncher.launch(options);
        });

        btnConfirmJoin.setOnClickListener(v -> {
            String token = etRoomToken.getText().toString().trim();
            if (TextUtils.isEmpty(token)) {
                etRoomToken.setError("Vui lòng nhập mã token của phòng");
                etRoomToken.requestFocus();
                return;
            }
            handleJoinWithToken(token);
        });
    }

    private void handleJoinWithToken(String token) {
        if (TextUtils.isEmpty(token)) return;

        btnConfirmJoin.setEnabled(false);
        btnScanQr.setEnabled(false);

        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            Room room = db.roomDao().getRoomByToken(token);
            if (room == null) {
                // Also check if token matches roomNumber directly for flexibility
                room = db.roomDao().getRoomByNumber(token);
            }

            final Room finalRoom = room;
            runOnUiThread(() -> {
                btnConfirmJoin.setEnabled(true);
                btnScanQr.setEnabled(true);

                if (finalRoom == null) {
                    Toast.makeText(JoinRoomActivity.this,
                            "Mã phòng không tồn tại hoặc không hợp lệ: " + token,
                            Toast.LENGTH_LONG).show();
                } else {
                    processJoinRoom(finalRoom);
                }
            });
        });
    }

    private void processJoinRoom(Room room) {
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            long userId = sessionManager.getUserId();
            User user = db.userDao().getUserById(userId);

            if (user != null) {
                user.setRoomId(room.getId());
                db.userDao().update(user);
            }

            // Update room status
            room.setStatus("DANG_THUE");
            db.roomDao().update(room);

            // Check / create active contract
            Contract activeContract = db.contractDao().getActiveContractByRoom(room.getId());
            if (activeContract == null) {
                long now = System.currentTimeMillis();
                long sixMonthsLater = now + (180L * 24 * 60 * 60 * 1000);
                Contract newContract = new Contract(
                        room.getId(),
                        userId,
                        now,
                        sixMonthsLater,
                        room.getBasePrice(),
                        true
                );
                db.contractDao().insert(newContract);
            }

            // Update session
            sessionManager.updateRoomId(room.getId());

            runOnUiThread(() -> {
                Toast.makeText(JoinRoomActivity.this,
                        "Vào phòng " + room.getRoomNumber() + " thành công!",
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(JoinRoomActivity.this, TenantDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}
