package com.example.myapplication.ui.owner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.entity.Room;
import com.google.android.material.card.MaterialCardView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    public static class RoomItem {
        public Room room;
        public String tenantName;
        public boolean hasDebt;

        public RoomItem(Room room, String tenantName, boolean hasDebt) {
            this.room = room;
            this.tenantName = tenantName;
            this.hasDebt = hasDebt;
        }
    }

    public interface OnRoomActionListener {
        void onRoomClick(Room room);
        void onEditClick(Room room);
        void onDeleteClick(Room room);
    }

    private final List<RoomItem> items = new ArrayList<>();
    private final OnRoomActionListener listener;
    private final DecimalFormat df = new DecimalFormat("#,###");

    public RoomAdapter(OnRoomActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<RoomItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        RoomItem item = items.get(position);
        Room room = item.room;

        holder.tvItemRoomNumber.setText("Phòng " + room.getRoomNumber());
        holder.tvItemRoomPrice.setText(df.format(room.getBasePrice()) + " đ/tháng");
        holder.tvItemBikeCount.setText(room.getCurrentBikeCount() + " xe");
        holder.tvItemToken.setText(room.getQrJoinToken() != null ? room.getQrJoinToken() : "—");

        // Status badge
        boolean isOccupied = "DANG_THUE".equals(room.getStatus());
        if (isOccupied) {
            holder.tvItemRoomStatus.setText("ĐANG THUÊ");
            holder.tvItemRoomStatus.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_badge_paid));
            holder.tvItemRoomStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_paid));
        } else {
            holder.tvItemRoomStatus.setText("TRỐNG");
            holder.tvItemRoomStatus.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_badge_vacant));
            holder.tvItemRoomStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
        }

        // Debt badge
        if (item.hasDebt) {
            holder.tvItemRoomDebt.setVisibility(View.VISIBLE);
        } else {
            holder.tvItemRoomDebt.setVisibility(View.GONE);
        }

        // Tenant name
        if (item.tenantName != null && !item.tenantName.trim().isEmpty()) {
            holder.tvItemTenantName.setText(item.tenantName);
            holder.tvItemTenantName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        } else {
            holder.tvItemTenantName.setText("Chưa có người ở");
            holder.tvItemTenantName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
        }

        // Clicks
        holder.cardRoomItem.setOnClickListener(v -> {
            if (listener != null) listener.onRoomClick(room);
        });

        holder.btnItemDetailRoom.setOnClickListener(v -> {
            if (listener != null) listener.onRoomClick(room);
        });

        holder.btnItemEditRoom.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(room);
        });

        holder.btnItemDeleteRoom.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(room);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardRoomItem;
        TextView tvItemRoomNumber;
        TextView tvItemRoomStatus;
        TextView tvItemRoomDebt;
        TextView tvItemRoomPrice;
        TextView tvItemBikeCount;
        TextView tvItemTenantName;
        TextView tvItemToken;
        Button btnItemDeleteRoom;
        Button btnItemEditRoom;
        Button btnItemDetailRoom;

        RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoomItem = itemView.findViewById(R.id.cardRoomItem);
            tvItemRoomNumber = itemView.findViewById(R.id.tvItemRoomNumber);
            tvItemRoomStatus = itemView.findViewById(R.id.tvItemRoomStatus);
            tvItemRoomDebt = itemView.findViewById(R.id.tvItemRoomDebt);
            tvItemRoomPrice = itemView.findViewById(R.id.tvItemRoomPrice);
            tvItemBikeCount = itemView.findViewById(R.id.tvItemBikeCount);
            tvItemTenantName = itemView.findViewById(R.id.tvItemTenantName);
            tvItemToken = itemView.findViewById(R.id.tvItemToken);
            btnItemDeleteRoom = itemView.findViewById(R.id.btnItemDeleteRoom);
            btnItemEditRoom = itemView.findViewById(R.id.btnItemEditRoom);
            btnItemDetailRoom = itemView.findViewById(R.id.btnItemDetailRoom);
        }
    }
}
