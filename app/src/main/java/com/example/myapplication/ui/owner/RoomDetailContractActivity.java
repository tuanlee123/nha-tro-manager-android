package com.example.myapplication.ui.owner;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.data.database.AppDatabase;
import com.example.myapplication.data.entity.Contract;
import com.example.myapplication.data.entity.Room;
import com.example.myapplication.data.entity.User;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class RoomDetailContractActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_ID = "EXTRA_ROOM_ID";

    private ImageButton btnBackDetail;
    private TextView tvDetailTitle;
    private TextView tvDetailRoomPrice;
    private TextView tvDetailRoomStatus;

    // Card 1: QR
    private ImageView ivRoomQrCode;
    private TextView tvDetailQrToken;
    private MaterialButton btnRefreshQr;

    // Card 2: Vacant State
    private LinearLayout layoutVacantState;
    private MaterialButton btnCreateContract;

    // Card 2: Occupied State
    private LinearLayout layoutOccupiedState;
    private TextView tvDetailTenantName;
    private TextView tvDetailTenantPhone;
    private TextView tvDetailTenantIdCard;
    private TextView tvDetailStartDate;
    private TextView tvDetailEndDate;
    private TextView tvDetailDeposit;
    private TextView tvDetailDaysRemaining;
    private MaterialButton btnUpdateContract;
    private MaterialButton btnLiquidateContract;

    private AppDatabase db;
    private long roomId = -1L;
    private Room room;
    private Contract activeContract;
    private User currentTenant;

    private final DecimalFormat df = new DecimalFormat("#,###");
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail_contract);

        db = AppDatabase.getInstance(this);
        roomId = getIntent().getLongExtra(EXTRA_ROOM_ID, -1L);

        if (roomId <= 0) {
            Toast.makeText(this, "Không tìm thấy phòng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadRoomAndContractData();
    }

    private void initViews() {
        btnBackDetail = findViewById(R.id.btnBackDetail);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailRoomPrice = findViewById(R.id.tvDetailRoomPrice);
        tvDetailRoomStatus = findViewById(R.id.tvDetailRoomStatus);

        ivRoomQrCode = findViewById(R.id.ivRoomQrCode);
        tvDetailQrToken = findViewById(R.id.tvDetailQrToken);
        btnRefreshQr = findViewById(R.id.btnRefreshQr);

        layoutVacantState = findViewById(R.id.layoutVacantState);
        btnCreateContract = findViewById(R.id.btnCreateContract);

        layoutOccupiedState = findViewById(R.id.layoutOccupiedState);
        tvDetailTenantName = findViewById(R.id.tvDetailTenantName);
        tvDetailTenantPhone = findViewById(R.id.tvDetailTenantPhone);
        tvDetailTenantIdCard = findViewById(R.id.tvDetailTenantIdCard);
        tvDetailStartDate = findViewById(R.id.tvDetailStartDate);
        tvDetailEndDate = findViewById(R.id.tvDetailEndDate);
        tvDetailDeposit = findViewById(R.id.tvDetailDeposit);
        tvDetailDaysRemaining = findViewById(R.id.tvDetailDaysRemaining);
        btnUpdateContract = findViewById(R.id.btnUpdateContract);
        btnLiquidateContract = findViewById(R.id.btnLiquidateContract);
    }

    private void setupListeners() {
        btnBackDetail.setOnClickListener(v -> finish());
        btnRefreshQr.setOnClickListener(v -> confirmRefreshQr());
        btnCreateContract.setOnClickListener(v -> showCreateContractDialog());
        btnUpdateContract.setOnClickListener(v -> showUpdateContractDialog());
        btnLiquidateContract.setOnClickListener(v -> confirmLiquidateContract());
    }

    private void loadRoomAndContractData() {
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            room = db.roomDao().getRoomById(roomId);
            if (room == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Phòng không tồn tại!", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            activeContract = db.contractDao().getActiveContractByRoom(roomId);
            currentTenant = null;
            if (activeContract != null) {
                currentTenant = db.userDao().getUserById(activeContract.getTenantId());
            }

            runOnUiThread(this::bindDataToViews);
        });
    }

    private void bindDataToViews() {
        if (room == null) return;

        tvDetailTitle.setText("Chi Tiết Phòng " + room.getRoomNumber());
        tvDetailRoomPrice.setText("Giá thuê: " + df.format(room.getBasePrice()) + " đ/tháng");

        boolean isOccupied = "DANG_THUE".equals(room.getStatus());
        if (isOccupied) {
            tvDetailRoomStatus.setText("ĐANG THUÊ");
            tvDetailRoomStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_paid));
            tvDetailRoomStatus.setTextColor(ContextCompat.getColor(this, R.color.status_paid));
            layoutVacantState.setVisibility(View.GONE);
            layoutOccupiedState.setVisibility(View.VISIBLE);
        } else {
            tvDetailRoomStatus.setText("TRỐNG");
            tvDetailRoomStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_vacant));
            tvDetailRoomStatus.setTextColor(ContextCompat.getColor(this, R.color.primary));
            layoutVacantState.setVisibility(View.VISIBLE);
            layoutOccupiedState.setVisibility(View.GONE);
        }

        // Render QR Code
        String token = room.getQrJoinToken();
        tvDetailQrToken.setText(token != null ? token : "—");
        renderQrCodeBitmap(token);

        // Bind Occupied Data
        if (isOccupied) {
            if (currentTenant != null) {
                tvDetailTenantName.setText(currentTenant.getFullName() != null ? currentTenant.getFullName() : "—");
                tvDetailTenantPhone.setText(currentTenant.getPhone() != null ? currentTenant.getPhone() : "—");
                tvDetailTenantIdCard.setText(currentTenant.getIdCard() != null ? currentTenant.getIdCard() : "—");
            } else {
                tvDetailTenantName.setText("Khách thuê (ID: " + (activeContract != null ? activeContract.getTenantId() : "—") + ")");
                tvDetailTenantPhone.setText("—");
                tvDetailTenantIdCard.setText("—");
            }

            if (activeContract != null) {
                tvDetailStartDate.setText(sdf.format(new Date(activeContract.getStartDate())));
                tvDetailEndDate.setText(sdf.format(new Date(activeContract.getEndDate())));
                tvDetailDeposit.setText(df.format(activeContract.getDepositAmount()) + " đ");

                long now = System.currentTimeMillis();
                long remainingMs = activeContract.getEndDate() - now;
                long remainingDays = remainingMs / (24L * 60 * 60 * 1000L);

                if (remainingDays < 0) {
                    tvDetailDaysRemaining.setText("⚠️ Đã quá hạn " + Math.abs(remainingDays) + " ngày!");
                    tvDetailDaysRemaining.setBackgroundColor(ContextCompat.getColor(this, R.color.status_unpaid_bg));
                    tvDetailDaysRemaining.setTextColor(ContextCompat.getColor(this, R.color.status_unpaid));
                } else if (remainingDays <= 15) {
                    tvDetailDaysRemaining.setText("⚠️ Sắp hết hạn! Còn " + remainingDays + " ngày");
                    tvDetailDaysRemaining.setBackgroundColor(0xFFFEF3C7);
                    tvDetailDaysRemaining.setTextColor(0xFFB45309);
                } else {
                    tvDetailDaysRemaining.setText("⏳ Còn " + remainingDays + " ngày nữa hết hạn");
                    tvDetailDaysRemaining.setBackgroundColor(0xFFE8F5E9);
                    tvDetailDaysRemaining.setTextColor(ContextCompat.getColor(this, R.color.status_paid));
                }
            }
        }
    }

    private void renderQrCodeBitmap(String token) {
        if (token == null || token.trim().isEmpty()) {
            ivRoomQrCode.setImageBitmap(null);
            return;
        }

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(token, BarcodeFormat.QR_CODE, 400, 400);
            ivRoomQrCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể tạo ảnh QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmRefreshQr() {
        new AlertDialog.Builder(this)
                .setTitle("Đổi mã QR mới")
                .setMessage("Bạn có chắc chắn muốn đổi mã QR và Token cho phòng này? Khách mới sẽ cần mã mới này để quét vào phòng.")
                .setPositiveButton("Đổi mã", (dialog, which) -> {
                    String cleanNum = room.getRoomNumber().replaceAll("[^a-zA-Z0-9]", "");
                    String newToken = "JOIN" + cleanNum + "_" + (1000 + new Random().nextInt(9000));
                    room.setQrJoinToken(newToken);

                    AppDatabase.getDatabaseWriteExecutor().execute(() -> {
                        db.roomDao().update(room);
                        runOnUiThread(() -> {
                            tvDetailQrToken.setText(newToken);
                            renderQrCodeBitmap(newToken);
                            Toast.makeText(this, "Đã cập nhật mã QR mới: " + newToken, Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showCreateContractDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lập Hợp Đồng Thuê");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etTenantName = new EditText(this);
        etTenantName.setHint("Họ và tên khách thuê");
        layout.addView(etTenantName);

        final EditText etTenantPhone = new EditText(this);
        etTenantPhone.setHint("Số điện thoại khách");
        layout.addView(etTenantPhone);

        final EditText etTenantIdCard = new EditText(this);
        etTenantIdCard.setHint("Số CCCD khách thuê");
        layout.addView(etTenantIdCard);

        final EditText etDeposit = new EditText(this);
        etDeposit.setHint("Tiền đặt cọc (VNĐ)");
        etDeposit.setText(String.valueOf((long) room.getBasePrice()));
        layout.addView(etDeposit);

        final Calendar calStart = Calendar.getInstance();
        final Calendar calEnd = Calendar.getInstance();
        calEnd.add(Calendar.MONTH, 6);

        final TextView tvDateRange = new TextView(this);
        tvDateRange.setPadding(0, 20, 0, 10);
        tvDateRange.setText("Thời hạn: " + sdf.format(calStart.getTime()) + " - " + sdf.format(calEnd.getTime()) + " (Bấm để đổi)");
        tvDateRange.setTextColor(ContextCompat.getColor(this, R.color.primary));
        layout.addView(tvDateRange);

        tvDateRange.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calEnd.set(year, month, dayOfMonth);
                tvDateRange.setText("Thời hạn: " + sdf.format(calStart.getTime()) + " - " + sdf.format(calEnd.getTime()) + " (Bấm để đổi)");
            }, calEnd.get(Calendar.YEAR), calEnd.get(Calendar.MONTH), calEnd.get(Calendar.DAY_OF_MONTH)).show();
        });

        builder.setView(layout);
        builder.setPositiveButton("LẬP HỢP ĐỒNG", (dialog, which) -> {
            String name = etTenantName.getText().toString().trim();
            String phone = etTenantPhone.getText().toString().trim();
            String idCard = etTenantIdCard.getText().toString().trim();
            String depStr = etDeposit.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên và số điện thoại khách!", Toast.LENGTH_SHORT).show();
                return;
            }

            double deposit = 0.0;
            try {
                deposit = Double.parseDouble(depStr);
            } catch (Exception ignored) {
            }

            final double finalDeposit = deposit;

            AppDatabase.getDatabaseWriteExecutor().execute(() -> {
                User tenant = db.userDao().getUserByPhone(phone);
                long tenantId;
                if (tenant == null) {
                    tenant = new User(name, phone, "123456", "TENANT", idCard, null, null, null, null, room.getId());
                    tenantId = db.userDao().insert(tenant);
                } else {
                    tenant.setFullName(name);
                    tenant.setIdCard(idCard);
                    tenant.setRoomId(room.getId());
                    db.userDao().update(tenant);
                    tenantId = tenant.getId();
                }

                // Deactivate any existing contracts for this room
                Contract old = db.contractDao().getActiveContractByRoom(room.getId());
                if (old != null) {
                    old.setActive(false);
                    db.contractDao().update(old);
                }

                // Create new contract
                Contract newContract = new Contract(
                        room.getId(),
                        tenantId,
                        calStart.getTimeInMillis(),
                        calEnd.getTimeInMillis(),
                        finalDeposit,
                        true
                );
                db.contractDao().insert(newContract);

                room.setStatus("DANG_THUE");
                db.roomDao().update(room);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã lập hợp đồng thành công!", Toast.LENGTH_SHORT).show();
                    loadRoomAndContractData();
                });
            });
        });
        builder.setNegativeButton("HỦY", null);
        builder.show();
    }

    private void showUpdateContractDialog() {
        if (activeContract == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập Nhật Hợp Đồng");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final Calendar calStart = Calendar.getInstance();
        calStart.setTimeInMillis(activeContract.getStartDate());

        final Calendar calEnd = Calendar.getInstance();
        calEnd.setTimeInMillis(activeContract.getEndDate());

        final TextView tvStartDate = new TextView(this);
        tvStartDate.setPadding(0, 16, 0, 16);
        tvStartDate.setText("Ngày bắt đầu: " + sdf.format(calStart.getTime()) + " (Bấm để đổi)");
        layout.addView(tvStartDate);

        tvStartDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calStart.set(year, month, dayOfMonth);
                tvStartDate.setText("Ngày bắt đầu: " + sdf.format(calStart.getTime()) + " (Bấm để đổi)");
            }, calStart.get(Calendar.YEAR), calStart.get(Calendar.MONTH), calStart.get(Calendar.DAY_OF_MONTH)).show();
        });

        final TextView tvEndDate = new TextView(this);
        tvEndDate.setPadding(0, 16, 0, 16);
        tvEndDate.setText("Ngày kết thúc: " + sdf.format(calEnd.getTime()) + " (Bấm để đổi)");
        layout.addView(tvEndDate);

        tvEndDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calEnd.set(year, month, dayOfMonth);
                tvEndDate.setText("Ngày kết thúc: " + sdf.format(calEnd.getTime()) + " (Bấm để đổi)");
            }, calEnd.get(Calendar.YEAR), calEnd.get(Calendar.MONTH), calEnd.get(Calendar.DAY_OF_MONTH)).show();
        });

        final EditText etDeposit = new EditText(this);
        etDeposit.setHint("Tiền đặt cọc (VNĐ)");
        etDeposit.setText(String.valueOf((long) activeContract.getDepositAmount()));
        layout.addView(etDeposit);

        builder.setView(layout);
        builder.setPositiveButton("LƯU THAY ĐỔI", (dialog, which) -> {
            double deposit = activeContract.getDepositAmount();
            try {
                deposit = Double.parseDouble(etDeposit.getText().toString().trim());
            } catch (Exception ignored) {
            }

            activeContract.setStartDate(calStart.getTimeInMillis());
            activeContract.setEndDate(calEnd.getTimeInMillis());
            activeContract.setDepositAmount(deposit);

            AppDatabase.getDatabaseWriteExecutor().execute(() -> {
                db.contractDao().update(activeContract);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã cập nhật hợp đồng!", Toast.LENGTH_SHORT).show();
                    loadRoomAndContractData();
                });
            });
        });
        builder.setNegativeButton("HỦY", null);
        builder.show();
    }

    private void confirmLiquidateContract() {
        new AlertDialog.Builder(this)
                .setTitle("Thanh lý & Trả phòng")
                .setMessage("Bạn có chắc chắn muốn thanh lý hợp đồng và trả phòng " + room.getRoomNumber() + "? Trạng thái phòng sẽ chuyển về TRỐNG và mã QR nhận phòng sẽ được tự động đổi mới.")
                .setPositiveButton("Xác nhận trả phòng", (dialog, which) -> {
                    AppDatabase.getDatabaseWriteExecutor().execute(() -> {
                        // Deactivate contract
                        if (activeContract != null) {
                            activeContract.setActive(false);
                            db.contractDao().update(activeContract);
                        }

                        // Remove roomId from current tenant
                        if (currentTenant != null) {
                            currentTenant.setRoomId(null);
                            db.userDao().update(currentTenant);
                        }

                        // Regenerate QR join token & set status TRONG
                        String cleanNum = room.getRoomNumber().replaceAll("[^a-zA-Z0-9]", "");
                        String newToken = "JOIN" + cleanNum + "_" + (1000 + new Random().nextInt(9000));
                        room.setStatus("TRONG");
                        room.setQrJoinToken(newToken);
                        db.roomDao().update(room);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Đã thanh lý hợp đồng và trả phòng thành công!", Toast.LENGTH_SHORT).show();
                            loadRoomAndContractData();
                        });
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
