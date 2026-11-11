package com.example.myapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.database.AppDatabase;
import com.example.myapplication.data.entity.User;
import com.example.myapplication.ui.owner.OwnerMainActivity;
import com.example.myapplication.ui.tenant.JoinRoomActivity;
import com.example.myapplication.ui.tenant.TenantDashboardActivity;
import com.example.myapplication.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etPhone;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private Button btnSeedData;

    private AppDatabase db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        db = AppDatabase.getInstance(this);

        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateByRole(sessionManager.getRole(), sessionManager.getRoomId());
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        // Ensure database has default test data
        AppDatabase.getDatabaseWriteExecutor().execute(() -> AppDatabase.seedInitialData(db));

        initViews();
        setupListeners();
    }

    private void initViews() {
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        btnSeedData = findViewById(R.id.btnSeedData);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnSeedData.setOnClickListener(v -> {
            AppDatabase.getDatabaseWriteExecutor().execute(() -> {
                db.clearAllTables();
                AppDatabase.seedInitialData(db);
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this,
                            "Đã khởi tạo lại toàn bộ dữ liệu mẫu!\nChủ trọ: 0900000001 / 123456\nKhách thuê: 0900000002 / 123456\nMã phòng: JOIN101",
                            Toast.LENGTH_LONG).show();
                    etPhone.setText("0900000001");
                    etPassword.setText("123456");
                });
            });
        });
    }

    private void handleLogin() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);

        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            User user = db.userDao().login(phone, password);
            runOnUiThread(() -> {
                btnLogin.setEnabled(true);
                if (user == null) {
                    Toast.makeText(LoginActivity.this,
                            "Số điện thoại hoặc mật khẩu không chính xác!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    sessionManager.createLoginSession(
                            user.getId(),
                            user.getRole(),
                            user.getRoomId(),
                            user.getFullName(),
                            user.getPhone()
                    );
                    Toast.makeText(LoginActivity.this,
                            "Đăng nhập thành công! Xin chào " + user.getFullName(),
                            Toast.LENGTH_SHORT).show();
                    navigateByRole(user.getRole(), user.getRoomId() != null ? user.getRoomId() : -1L);
                    finish();
                }
            });
        });
    }

    private void navigateByRole(String role, long roomId) {
        if ("OWNER".equalsIgnoreCase(role)) {
            Intent intent = new Intent(this, OwnerMainActivity.class);
            startActivity(intent);
        } else {
            // Role TENANT
            if (roomId <= 0) {
                Intent intent = new Intent(this, JoinRoomActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, TenantDashboardActivity.class);
                startActivity(intent);
            }
        }
    }
}
