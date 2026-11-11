package com.example.myapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.database.AppDatabase;
import com.example.myapplication.data.entity.User;
import com.example.myapplication.ui.owner.OwnerMainActivity;
import com.example.myapplication.ui.tenant.JoinRoomActivity;
import com.example.myapplication.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private static class BankItem {
        final String name;
        final String bin;

        BankItem(String name, String bin) {
            this.name = name;
            this.bin = bin;
        }

        @Override
        public String toString() {
            return name + " (" + bin + ")";
        }
    }

    private RadioGroup rgRole;
    private LinearLayout llTenantFields;
    private LinearLayout llOwnerFields;

    private EditText etFullName;
    private EditText etPhone;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private EditText etIdCard;

    private EditText etBoardingHouseName;
    private Spinner spBank;
    private EditText etBankAccountNo;
    private EditText etBankAccountName;

    private Button btnRegister;
    private TextView tvGoToLogin;

    private AppDatabase db;
    private SessionManager sessionManager;
    private final List<BankItem> bankList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupBankSpinner();
        setupListeners();
    }

    private void initViews() {
        rgRole = findViewById(R.id.rgRole);
        llTenantFields = findViewById(R.id.llTenantFields);
        llOwnerFields = findViewById(R.id.llOwnerFields);

        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etIdCard = findViewById(R.id.etIdCard);

        etBoardingHouseName = findViewById(R.id.etBoardingHouseName);
        spBank = findViewById(R.id.spBank);
        etBankAccountNo = findViewById(R.id.etBankAccountNo);
        etBankAccountName = findViewById(R.id.etBankAccountName);

        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
    }

    private void setupBankSpinner() {
        bankList.add(new BankItem("MB Bank (Quân Đội)", "970422"));
        bankList.add(new BankItem("Vietcombank", "970436"));
        bankList.add(new BankItem("Techcombank", "970407"));
        bankList.add(new BankItem("BIDV", "970418"));
        bankList.add(new BankItem("VietinBank", "970415"));
        bankList.add(new BankItem("VPBank", "970432"));
        bankList.add(new BankItem("ACB", "970416"));
        bankList.add(new BankItem("TPBank", "970423"));
        bankList.add(new BankItem("Sacombank", "970403"));
        bankList.add(new BankItem("HDBank", "970437"));

        ArrayAdapter<BankItem> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                bankList
        );
        spBank.setAdapter(adapter);
    }

    private void setupListeners() {
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbOwner) {
                llOwnerFields.setVisibility(View.VISIBLE);
                llTenantFields.setVisibility(View.GONE);
            } else {
                llOwnerFields.setVisibility(View.GONE);
                llTenantFields.setVisibility(View.VISIBLE);
            }
        });

        btnRegister.setOnClickListener(v -> handleRegister());

        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        boolean isOwner = rgRole.getCheckedRadioButtonId() == R.id.rbOwner;
        String role = isOwner ? "OWNER" : "TENANT";

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Vui lòng nhập họ và tên");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        String idCard = null;
        String boardingHouseName = null;
        String bankBin = null;
        String bankAccountNo = null;
        String bankAccountName = null;

        if (isOwner) {
            boardingHouseName = etBoardingHouseName.getText().toString().trim();
            bankAccountNo = etBankAccountNo.getText().toString().trim();
            bankAccountName = etBankAccountName.getText().toString().trim().toUpperCase();

            if (TextUtils.isEmpty(boardingHouseName)) {
                etBoardingHouseName.setError("Vui lòng nhập tên khu trọ");
                etBoardingHouseName.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(bankAccountNo)) {
                etBankAccountNo.setError("Vui lòng nhập số tài khoản");
                etBankAccountNo.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(bankAccountName)) {
                etBankAccountName.setError("Vui lòng nhập tên chủ tài khoản");
                etBankAccountName.requestFocus();
                return;
            }

            BankItem selectedBank = (BankItem) spBank.getSelectedItem();
            bankBin = selectedBank != null ? selectedBank.bin : "970422";
        } else {
            idCard = etIdCard.getText().toString().trim();
            if (TextUtils.isEmpty(idCard)) {
                etIdCard.setError("Vui lòng nhập số CCCD / CMND");
                etIdCard.requestFocus();
                return;
            }
        }

        btnRegister.setEnabled(false);

        final String finalIdCard = idCard;
        final String finalBoardingHouseName = boardingHouseName;
        final String finalBankBin = bankBin;
        final String finalBankAccountNo = bankAccountNo;
        final String finalBankAccountName = bankAccountName;

        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            User existing = db.userDao().getUserByPhone(phone);
            if (existing != null) {
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this,
                            "Số điện thoại này đã được đăng ký tài khoản!",
                            Toast.LENGTH_LONG).show();
                });
                return;
            }

            User newUser = new User(
                    fullName,
                    phone,
                    password,
                    role,
                    finalIdCard,
                    finalBoardingHouseName,
                    finalBankBin,
                    finalBankAccountNo,
                    finalBankAccountName,
                    null
            );

            long newId = db.userDao().insert(newUser);
            newUser.setId(newId);

            runOnUiThread(() -> {
                btnRegister.setEnabled(true);
                sessionManager.createLoginSession(newId, role, null, fullName, phone);

                Toast.makeText(RegisterActivity.this,
                        "Đăng ký tài khoản thành công!",
                        Toast.LENGTH_SHORT).show();

                if (isOwner) {
                    Intent intent = new Intent(RegisterActivity.this, OwnerMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(RegisterActivity.this, JoinRoomActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                finish();
            });
        });
    }
}
