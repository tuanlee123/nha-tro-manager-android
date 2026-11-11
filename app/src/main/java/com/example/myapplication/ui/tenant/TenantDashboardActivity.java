package com.example.myapplication.ui.tenant;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.database.AppDatabase;
import com.example.myapplication.data.entity.Invoice;
import com.example.myapplication.data.entity.Room;
import com.example.myapplication.data.entity.User;
import com.example.myapplication.ui.auth.LoginActivity;
import com.example.myapplication.utils.SessionManager;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Locale;

public class TenantDashboardActivity extends AppCompatActivity {

    // Views
    private TextView tvHelloTenant;
    private TextView tvBoardingHouseHeader;
    private Button btnLeaveRoom;
    private Button btnLogout;

    // Card 1 Views
    private TextView tvRoomNumberTitle;
    private TextView tvRoomStatusBadge;
    private TextView tvRoomBasePrice;
    private TextView tvElecUnitPrice;
    private TextView tvWaterUnitPrice;
    private TextView tvServiceFee;
    private TextView tvBikeUnitPrice;
    private Button btnMinusBike;
    private TextView tvCurrentBikeCount;
    private Button btnPlusBike;
    private Button btnSaveBikeCount;

    // Card 2 Views
    private TextView tvInvoiceTitle;
    private TextView tvInvoiceStatusBadge;
    private LinearLayout layoutInvoiceContent;
    private TextView tvInvoiceRoomPrice;
    private TextView tvInvoiceElecDetail;
    private TextView tvInvoiceElecTotal;
    private TextView tvInvoiceWaterDetail;
    private TextView tvInvoiceWaterTotal;
    private TextView tvInvoiceServiceTotal;
    private TextView tvInvoiceBikeTitle;
    private TextView tvInvoiceBikeTotal;
    private TextView tvInvoiceTotalAmount;
    private TextView tvNoInvoiceMessage;

    // Card 3 Views
    private View cardVietQr;
    private LinearLayout layoutPaymentQr;
    private TextView tvPaymentBankInfo;
    private TextView tvPaymentAccountName;
    private ImageView ivVietQr;
    private TextView tvQrAddInfo;
    private Button btnSaveQr;
    private LinearLayout layoutPaidNotice;

    // Data & state
    private AppDatabase db;
    private SessionManager sessionManager;
    private Room currentRoom;
    private Invoice currentInvoice;
    private User currentOwner;
    private int tempBikeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_dashboard);

        db = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
        loadDashboardData();
    }

    private void initViews() {
        tvHelloTenant = findViewById(R.id.tvHelloTenant);
        tvBoardingHouseHeader = findViewById(R.id.tvBoardingHouseHeader);
        btnLeaveRoom = findViewById(R.id.btnLeaveRoom);
        btnLogout = findViewById(R.id.btnLogout);

        // Card 1
        tvRoomNumberTitle = findViewById(R.id.tvRoomNumberTitle);
        tvRoomStatusBadge = findViewById(R.id.tvRoomStatusBadge);
        tvRoomBasePrice = findViewById(R.id.tvRoomBasePrice);
        tvElecUnitPrice = findViewById(R.id.tvElecUnitPrice);
        tvWaterUnitPrice = findViewById(R.id.tvWaterUnitPrice);
        tvServiceFee = findViewById(R.id.tvServiceFee);
        tvBikeUnitPrice = findViewById(R.id.tvBikeUnitPrice);
        btnMinusBike = findViewById(R.id.btnMinusBike);
        tvCurrentBikeCount = findViewById(R.id.tvCurrentBikeCount);
        btnPlusBike = findViewById(R.id.btnPlusBike);
        btnSaveBikeCount = findViewById(R.id.btnSaveBikeCount);

        // Card 2
        tvInvoiceTitle = findViewById(R.id.tvInvoiceTitle);
        tvInvoiceStatusBadge = findViewById(R.id.tvInvoiceStatusBadge);
        layoutInvoiceContent = findViewById(R.id.layoutInvoiceContent);
        tvInvoiceRoomPrice = findViewById(R.id.tvInvoiceRoomPrice);
        tvInvoiceElecDetail = findViewById(R.id.tvInvoiceElecDetail);
        tvInvoiceElecTotal = findViewById(R.id.tvInvoiceElecTotal);
        tvInvoiceWaterDetail = findViewById(R.id.tvInvoiceWaterDetail);
        tvInvoiceWaterTotal = findViewById(R.id.tvInvoiceWaterTotal);
        tvInvoiceServiceTotal = findViewById(R.id.tvInvoiceServiceTotal);
        tvInvoiceBikeTitle = findViewById(R.id.tvInvoiceBikeTitle);
        tvInvoiceBikeTotal = findViewById(R.id.tvInvoiceBikeTotal);
        tvInvoiceTotalAmount = findViewById(R.id.tvInvoiceTotalAmount);
        tvNoInvoiceMessage = findViewById(R.id.tvNoInvoiceMessage);

        // Card 3
        cardVietQr = findViewById(R.id.cardVietQr);
        layoutPaymentQr = findViewById(R.id.layoutPaymentQr);
        tvPaymentBankInfo = findViewById(R.id.tvPaymentBankInfo);
        tvPaymentAccountName = findViewById(R.id.tvPaymentAccountName);
        ivVietQr = findViewById(R.id.ivVietQr);
        tvQrAddInfo = findViewById(R.id.tvQrAddInfo);
        btnSaveQr = findViewById(R.id.btnSaveQr);
        layoutPaidNotice = findViewById(R.id.layoutPaidNotice);

        tvHelloTenant.setText("Xin chào, " + sessionManager.getFullName());
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(TenantDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnLeaveRoom.setOnClickListener(v -> confirmLeaveRoom());

        btnMinusBike.setOnClickListener(v -> {
            if (tempBikeCount > 0) {
                tempBikeCount--;
                tvCurrentBikeCount.setText(String.valueOf(tempBikeCount));
            }
        });

        btnPlusBike.setOnClickListener(v -> {
            tempBikeCount++;
            tvCurrentBikeCount.setText(String.valueOf(tempBikeCount));
        });

        btnSaveBikeCount.setOnClickListener(v -> saveBikeCount());

        btnSaveQr.setOnClickListener(v -> saveQrImageToGallery());
    }

    private void loadDashboardData() {
        long roomId = sessionManager.getRoomId();
        if (roomId <= 0) {
            // Not in room, redirect to JoinRoomActivity
            Intent intent = new Intent(this, JoinRoomActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            currentRoom = db.roomDao().getRoomById(roomId);
            currentInvoice = db.invoiceDao().getLatestInvoiceByRoom(roomId);
            currentOwner = db.userDao().getFirstOwner();

            runOnUiThread(() -> {
                bindRoomData();
                bindInvoiceData();
                bindVietQrData();
            });
        });
    }

    private void bindRoomData() {
        if (currentRoom == null) return;

        tvRoomNumberTitle.setText("Phòng " + currentRoom.getRoomNumber());
        tvRoomBasePrice.setText(formatMoney(currentRoom.getBasePrice()) + " /tháng");
        tvElecUnitPrice.setText(formatMoney(currentRoom.getElecUnitPrice()) + " /kWh");
        tvWaterUnitPrice.setText(formatMoney(currentRoom.getWaterUnitPrice()) + " /khối");
        tvServiceFee.setText(formatMoney(currentRoom.getWifiFee() + currentRoom.getGarbageFee()) + " /tháng");
        tvBikeUnitPrice.setText(formatMoney(currentRoom.getBikeUnitPrice()) + " /xe/tháng");

        tempBikeCount = currentRoom.getCurrentBikeCount();
        tvCurrentBikeCount.setText(String.valueOf(tempBikeCount));

        if (currentOwner != null && currentOwner.getBoardingHouseName() != null) {
            tvBoardingHouseHeader.setText(currentOwner.getBoardingHouseName());
        }
    }

    private void bindInvoiceData() {
        if (currentInvoice == null) {
            tvInvoiceTitle.setText("Hóa đơn tháng này");
            layoutInvoiceContent.setVisibility(View.GONE);
            tvNoInvoiceMessage.setVisibility(View.VISIBLE);
            tvInvoiceStatusBadge.setVisibility(View.GONE);
            cardVietQr.setVisibility(View.GONE);
            return;
        }

        cardVietQr.setVisibility(View.VISIBLE);
        layoutInvoiceContent.setVisibility(View.VISIBLE);
        tvNoInvoiceMessage.setVisibility(View.GONE);
        tvInvoiceStatusBadge.setVisibility(View.VISIBLE);

        tvInvoiceTitle.setText(String.format(Locale.getDefault(), "Hóa đơn tháng %d/%d",
                currentInvoice.getMonth(), currentInvoice.getYear()));

        if (currentInvoice.isPaid()) {
            tvInvoiceStatusBadge.setText(R.string.status_paid);
            tvInvoiceStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_paid));
            tvInvoiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_paid);
        } else {
            tvInvoiceStatusBadge.setText(R.string.status_unpaid);
            tvInvoiceStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_unpaid));
            tvInvoiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_unpaid);
        }

        tvInvoiceRoomPrice.setText(formatMoney(currentInvoice.getRoomPrice()));

        int elecUsed = currentInvoice.getNewElec() - currentInvoice.getOldElec();
        tvInvoiceElecDetail.setText(String.format(Locale.getDefault(),
                "Số cũ: %d - Số mới: %d (%d kWh)",
                currentInvoice.getOldElec(), currentInvoice.getNewElec(), elecUsed));
        tvInvoiceElecTotal.setText(formatMoney(currentInvoice.getElecTotal()));

        int waterUsed = currentInvoice.getNewWater() - currentInvoice.getOldWater();
        tvInvoiceWaterDetail.setText(String.format(Locale.getDefault(),
                "Số cũ: %d - Số mới: %d (%d khối)",
                currentInvoice.getOldWater(), currentInvoice.getNewWater(), waterUsed));
        tvInvoiceWaterTotal.setText(formatMoney(currentInvoice.getWaterTotal()));

        tvInvoiceServiceTotal.setText(formatMoney(currentInvoice.getServiceFee()));

        tvInvoiceBikeTitle.setText(String.format(Locale.getDefault(),
                "5. Tiền giữ xe (%d xe):", currentInvoice.getBikeCount()));
        tvInvoiceBikeTotal.setText(formatMoney(currentInvoice.getBikeTotal()));

        tvInvoiceTotalAmount.setText(formatMoney(currentInvoice.getTotalAmount()));
    }

    private void bindVietQrData() {
        if (currentInvoice == null) {
            cardVietQr.setVisibility(View.GONE);
            return;
        }

        if (currentInvoice.isPaid()) {
            layoutPaymentQr.setVisibility(View.GONE);
            layoutPaidNotice.setVisibility(View.VISIBLE);
            return;
        }

        layoutPaymentQr.setVisibility(View.VISIBLE);
        layoutPaidNotice.setVisibility(View.GONE);

        String bankBin = (currentOwner != null && currentOwner.getBankBin() != null)
                ? currentOwner.getBankBin() : "970422";
        String accountNo = (currentOwner != null && currentOwner.getBankAccountNo() != null)
                ? currentOwner.getBankAccountNo() : "0123456789";
        String accountName = (currentOwner != null && currentOwner.getBankAccountName() != null)
                ? currentOwner.getBankAccountName() : "NGUYEN VAN CHU";

        tvPaymentBankInfo.setText(String.format("Chuyển khoản (BIN: %s) - STK: %s", bankBin, accountNo));
        tvPaymentAccountName.setText("Chủ tài khoản: " + accountName);

        String roomNum = currentRoom != null ? currentRoom.getRoomNumber() : "101";
        String addInfo = String.format(Locale.US, "P%s T%d%d",
                roomNum, currentInvoice.getMonth(), currentInvoice.getYear());
        tvQrAddInfo.setText("Nội dung chuyển khoản: " + addInfo);

        // Build VietQR image URL
        // https://img.vietqr.io/image/<BANK_BIN>-<ACCOUNT_NO>-compact2.png?amount=<TOTAL_AMOUNT>&addInfo=...&accountName=...
        try {
            long amount = Math.round(currentInvoice.getTotalAmount());
            String encodedAddInfo = URLEncoder.encode(addInfo, "UTF-8");
            String encodedAccountName = URLEncoder.encode(accountName, "UTF-8");

            String vietQrUrl = String.format(Locale.US,
                    "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                    bankBin, accountNo, amount, encodedAddInfo, encodedAccountName);

            Glide.with(this)
                    .load(vietQrUrl)
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(ivVietQr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveBikeCount() {
        if (currentRoom == null) return;

        btnSaveBikeCount.setEnabled(false);
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            currentRoom.setCurrentBikeCount(tempBikeCount);
            db.roomDao().update(currentRoom);

            runOnUiThread(() -> {
                btnSaveBikeCount.setEnabled(true);
                Toast.makeText(TenantDashboardActivity.this,
                        "Đã cập nhật số lượng xe thành công: " + tempBikeCount + " xe",
                        Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void confirmLeaveRoom() {
        new AlertDialog.Builder(this)
                .setTitle("Rời khỏi phòng")
                .setMessage("Bạn có chắc chắn muốn rời khỏi phòng này? Bạn sẽ cần quét lại mã QR để vào phòng.")
                .setPositiveButton("Rời phòng", (dialog, which) -> {
                    AppDatabase.getDatabaseWriteExecutor().execute(() -> {
                        long userId = sessionManager.getUserId();
                        User user = db.userDao().getUserById(userId);
                        if (user != null) {
                            user.setRoomId(null);
                            db.userDao().update(user);
                        }

                        if (currentRoom != null) {
                            currentRoom.setStatus("TRONG");
                            db.roomDao().update(currentRoom);
                        }

                        sessionManager.updateRoomId(-1L);

                        runOnUiThread(() -> {
                            Toast.makeText(TenantDashboardActivity.this,
                                    "Đã rời phòng thành công!",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(TenantDashboardActivity.this, JoinRoomActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        });
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveQrImageToGallery() {
        if (ivVietQr.getDrawable() == null || !(ivVietQr.getDrawable() instanceof BitmapDrawable)) {
            Toast.makeText(this, "Đang tải ảnh mã QR, vui lòng thử lại sau!", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = ((BitmapDrawable) ivVietQr.getDrawable()).getBitmap();
        if (bitmap == null) {
            Toast.makeText(this, "Không thể lấy hình ảnh mã QR!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveQr.setEnabled(false);
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            try {
                String filename = "VietQR_Phong_" + (currentRoom != null ? currentRoom.getRoomNumber() : "101")
                        + "_" + System.currentTimeMillis() + ".png";

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/VietQR");
                    values.put(MediaStore.Images.Media.IS_PENDING, 1);
                }

                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (imageUri != null) {
                    OutputStream fos = getContentResolver().openOutputStream(imageUri);
                    if (fos != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.close();
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear();
                        values.put(MediaStore.Images.Media.IS_PENDING, 0);
                        getContentResolver().update(imageUri, values, null, null);
                    }

                    runOnUiThread(() -> {
                        btnSaveQr.setEnabled(true);
                        Toast.makeText(TenantDashboardActivity.this,
                                "Đã lưu ảnh mã QR vào Thư viện thành công!\nBạn có thể mở app ngân hàng quét ảnh này.",
                                Toast.LENGTH_LONG).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        btnSaveQr.setEnabled(true);
                        Toast.makeText(TenantDashboardActivity.this, "Lưu ảnh thất bại!", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSaveQr.setEnabled(true);
                    Toast.makeText(TenantDashboardActivity.this, "Lỗi khi lưu ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String formatMoney(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount) + " đ";
    }
}
