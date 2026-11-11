package com.example.myapplication.ui.owner;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.database.AppDatabase;
import com.example.myapplication.data.entity.Room;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Random;

public class RoomFormActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_ID = "EXTRA_ROOM_ID";

    private ImageButton btnBackForm;
    private TextView tvFormTitle;
    private TextInputEditText etRoomNumber;
    private TextInputEditText etBasePrice;
    private TextInputEditText etElecPrice;
    private TextInputEditText etWaterPrice;
    private TextInputEditText etWifiFee;
    private TextInputEditText etGarbageFee;
    private TextInputEditText etBikeUnitPrice;
    private TextInputEditText etCurrentBikeCount;
    private TextInputEditText etQrToken;
    private MaterialButton btnSaveRoom;

    private AppDatabase db;
    private long roomId = -1L;
    private Room existingRoom = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_form);

        db = AppDatabase.getInstance(this);
        roomId = getIntent().getLongExtra(EXTRA_ROOM_ID, -1L);

        initViews();
        setupListeners();

        if (roomId > 0) {
            loadRoomData();
        }
    }

    private void initViews() {
        btnBackForm = findViewById(R.id.btnBackForm);
        tvFormTitle = findViewById(R.id.tvFormTitle);
        etRoomNumber = findViewById(R.id.etRoomNumber);
        etBasePrice = findViewById(R.id.etBasePrice);
        etElecPrice = findViewById(R.id.etElecPrice);
        etWaterPrice = findViewById(R.id.etWaterPrice);
        etWifiFee = findViewById(R.id.etWifiFee);
        etGarbageFee = findViewById(R.id.etGarbageFee);
        etBikeUnitPrice = findViewById(R.id.etBikeUnitPrice);
        etCurrentBikeCount = findViewById(R.id.etCurrentBikeCount);
        etQrToken = findViewById(R.id.etQrToken);
        btnSaveRoom = findViewById(R.id.btnSaveRoom);

        if (roomId > 0) {
            tvFormTitle.setText("Chỉnh Sửa Phòng");
        } else {
            tvFormTitle.setText("Thêm Phòng Mới");
        }
    }

    private void setupListeners() {
        btnBackForm.setOnClickListener(v -> finish());
        btnSaveRoom.setOnClickListener(v -> handleSaveRoom());
    }

    private void loadRoomData() {
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            existingRoom = db.roomDao().getRoomById(roomId);
            if (existingRoom != null) {
                runOnUiThread(() -> {
                    tvFormTitle.setText("Sửa Phòng " + existingRoom.getRoomNumber());
                    etRoomNumber.setText(existingRoom.getRoomNumber());
                    etBasePrice.setText(String.valueOf((long) existingRoom.getBasePrice()));
                    etElecPrice.setText(String.valueOf((long) existingRoom.getElecUnitPrice()));
                    etWaterPrice.setText(String.valueOf((long) existingRoom.getWaterUnitPrice()));
                    etWifiFee.setText(String.valueOf((long) existingRoom.getWifiFee()));
                    etGarbageFee.setText(String.valueOf((long) existingRoom.getGarbageFee()));
                    etBikeUnitPrice.setText(String.valueOf((long) existingRoom.getBikeUnitPrice()));
                    etCurrentBikeCount.setText(String.valueOf(existingRoom.getCurrentBikeCount()));
                    etQrToken.setText(existingRoom.getQrJoinToken());
                });
            }
        });
    }

    private void handleSaveRoom() {
        String roomNumber = etRoomNumber.getText() != null ? etRoomNumber.getText().toString().trim() : "";
        String basePriceStr = etBasePrice.getText() != null ? etBasePrice.getText().toString().trim() : "";
        String elecPriceStr = etElecPrice.getText() != null ? etElecPrice.getText().toString().trim() : "";
        String waterPriceStr = etWaterPrice.getText() != null ? etWaterPrice.getText().toString().trim() : "";
        String wifiFeeStr = etWifiFee.getText() != null ? etWifiFee.getText().toString().trim() : "";
        String garbageFeeStr = etGarbageFee.getText() != null ? etGarbageFee.getText().toString().trim() : "";
        String bikePriceStr = etBikeUnitPrice.getText() != null ? etBikeUnitPrice.getText().toString().trim() : "";
        String bikeCountStr = etCurrentBikeCount.getText() != null ? etCurrentBikeCount.getText().toString().trim() : "";
        String tokenStr = etQrToken.getText() != null ? etQrToken.getText().toString().trim() : "";

        if (roomNumber.isEmpty()) {
            etRoomNumber.setError("Vui lòng nhập số phòng");
            etRoomNumber.requestFocus();
            return;
        }

        if (basePriceStr.isEmpty()) {
            etBasePrice.setError("Vui lòng nhập giá phòng");
            etBasePrice.requestFocus();
            return;
        }

        double basePrice = parseDoubleOrDefault(basePriceStr, 0.0);
        double elecPrice = parseDoubleOrDefault(elecPriceStr, 3500.0);
        double waterPrice = parseDoubleOrDefault(waterPriceStr, 15000.0);
        double wifiFee = parseDoubleOrDefault(wifiFeeStr, 100000.0);
        double garbageFee = parseDoubleOrDefault(garbageFeeStr, 30000.0);
        double bikePrice = parseDoubleOrDefault(bikePriceStr, 100000.0);
        int bikeCount = parseIntOrDefault(bikeCountStr, 0);

        if (tokenStr.isEmpty()) {
            String cleanNum = roomNumber.replaceAll("[^a-zA-Z0-9]", "");
            tokenStr = "JOIN" + cleanNum + "_" + (1000 + new Random().nextInt(9000));
        }

        final String finalToken = tokenStr;

        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            Room duplicate = db.roomDao().getRoomByNumber(roomNumber);
            if (duplicate != null && (existingRoom == null || duplicate.getId() != existingRoom.getId())) {
                runOnUiThread(() -> {
                    etRoomNumber.setError("Số phòng này đã tồn tại!");
                    etRoomNumber.requestFocus();
                });
                return;
            }

            if (existingRoom == null) {
                // Insert new room
                Room newRoom = new Room(
                        roomNumber,
                        basePrice,
                        elecPrice,
                        waterPrice,
                        wifiFee,
                        garbageFee,
                        bikePrice,
                        bikeCount,
                        "TRONG",
                        finalToken
                );
                db.roomDao().insert(newRoom);
                runOnUiThread(() -> {
                    Toast.makeText(RoomFormActivity.this, "Đã thêm phòng " + roomNumber + " thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else {
                // Update existing room
                existingRoom.setRoomNumber(roomNumber);
                existingRoom.setBasePrice(basePrice);
                existingRoom.setElecUnitPrice(elecPrice);
                existingRoom.setWaterUnitPrice(waterPrice);
                existingRoom.setWifiFee(wifiFee);
                existingRoom.setGarbageFee(garbageFee);
                existingRoom.setBikeUnitPrice(bikePrice);
                existingRoom.setCurrentBikeCount(bikeCount);
                existingRoom.setQrJoinToken(finalToken);

                db.roomDao().update(existingRoom);
                runOnUiThread(() -> {
                    Toast.makeText(RoomFormActivity.this, "Cập nhật phòng " + roomNumber + " thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private double parseDoubleOrDefault(String val, double def) {
        try {
            return Double.parseDouble(val);
        } catch (Exception e) {
            return def;
        }
    }

    private int parseIntOrDefault(String val, int def) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return def;
        }
    }
}
