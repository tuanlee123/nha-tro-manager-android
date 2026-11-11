package com.example.myapplication.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myapplication.R;
import com.example.myapplication.data.database.AppDatabase;
import com.example.myapplication.data.entity.Contract;
import com.example.myapplication.data.entity.Room;
import com.example.myapplication.ui.owner.OwnerMainActivity;
import com.example.myapplication.ui.owner.RoomDetailContractActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ContractReminderWorker extends Worker {

    public static final String CHANNEL_ID = "CONTRACT_EXPIRY_CHANNEL";
    public static final String CHANNEL_NAME = "Nhắc hạn hợp đồng phòng trọ";

    public ContractReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        createNotificationChannel();

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        List<Contract> activeContracts = db.contractDao().getAllActiveContracts();
        if (activeContracts == null || activeContracts.isEmpty()) {
            return Result.success();
        }

        long now = System.currentTimeMillis();
        long days15Ms = 15L * 24 * 60 * 60 * 1000L;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (Contract contract : activeContracts) {
            long remaining = contract.getEndDate() - now;
            // Check if expiring in the next 15 days or overdue within 60 days
            if (remaining <= days15Ms && remaining >= -60L * 24 * 60 * 60 * 1000L) {
                Room room = db.roomDao().getRoomById(contract.getRoomId());
                String roomNumber = (room != null) ? room.getRoomNumber() : String.valueOf(contract.getRoomId());
                String expDateStr = sdf.format(new Date(contract.getEndDate()));

                String title;
                String content;
                if (remaining < 0) {
                    title = "Hợp đồng đã quá hạn!";
                    content = "Phòng " + roomNumber + " đã hết hạn từ ngày " + expDateStr + ". Vui lòng gia hạn hoặc thanh lý.";
                } else {
                    title = "Sắp hết hạn hợp đồng!";
                    content = "Phòng " + roomNumber + " sẽ hết hạn hợp đồng vào ngày " + expDateStr + ". Vui lòng gia hạn hoặc thanh lý.";
                }

                sendNotification((int) contract.getId(), title, content, contract.getRoomId());
            }
        }

        return Result.success();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo nhắc nhở khi hợp đồng thuê phòng sắp hết hạn");
            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void sendNotification(int notificationId, String title, String content, long roomId) {
        Context context = getApplicationContext();

        // Check POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Intent intent = new Intent(context, RoomDetailContractActivity.class);
        intent.putExtra("EXTRA_ROOM_ID", roomId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_custom_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        } catch (SecurityException ignored) {
        }
    }
}
