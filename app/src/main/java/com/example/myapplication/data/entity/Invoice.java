package com.example.myapplication.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "invoices")
public class Invoice {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long roomId;
    private int month;
    private int year;
    private int oldElec;
    private int newElec;
    private double elecTotal;
    private int oldWater;
    private int newWater;
    private double waterTotal;
    private int bikeCount;
    private double bikeTotal;
    private double serviceFee; // wifi + rác
    private double roomPrice;
    private double totalAmount;
    private boolean isPaid;
    private long paidAt;
    private long createdAt;

    public Invoice() {
    }

    public Invoice(long roomId, int month, int year, int oldElec, int newElec, double elecTotal,
                   int oldWater, int newWater, double waterTotal, int bikeCount, double bikeTotal,
                   double serviceFee, double roomPrice, double totalAmount, boolean isPaid,
                   long paidAt, long createdAt) {
        this.roomId = roomId;
        this.month = month;
        this.year = year;
        this.oldElec = oldElec;
        this.newElec = newElec;
        this.elecTotal = elecTotal;
        this.oldWater = oldWater;
        this.newWater = newWater;
        this.waterTotal = waterTotal;
        this.bikeCount = bikeCount;
        this.bikeTotal = bikeTotal;
        this.serviceFee = serviceFee;
        this.roomPrice = roomPrice;
        this.totalAmount = totalAmount;
        this.isPaid = isPaid;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getOldElec() {
        return oldElec;
    }

    public void setOldElec(int oldElec) {
        this.oldElec = oldElec;
    }

    public int getNewElec() {
        return newElec;
    }

    public void setNewElec(int newElec) {
        this.newElec = newElec;
    }

    public double getElecTotal() {
        return elecTotal;
    }

    public void setElecTotal(double elecTotal) {
        this.elecTotal = elecTotal;
    }

    public int getOldWater() {
        return oldWater;
    }

    public void setOldWater(int oldWater) {
        this.oldWater = oldWater;
    }

    public int getNewWater() {
        return newWater;
    }

    public void setNewWater(int newWater) {
        this.newWater = newWater;
    }

    public double getWaterTotal() {
        return waterTotal;
    }

    public void setWaterTotal(double waterTotal) {
        this.waterTotal = waterTotal;
    }

    public int getBikeCount() {
        return bikeCount;
    }

    public void setBikeCount(int bikeCount) {
        this.bikeCount = bikeCount;
    }

    public double getBikeTotal() {
        return bikeTotal;
    }

    public void setBikeTotal(double bikeTotal) {
        this.bikeTotal = bikeTotal;
    }

    public double getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(double serviceFee) {
        this.serviceFee = serviceFee;
    }

    public double getRoomPrice() {
        return roomPrice;
    }

    public void setRoomPrice(double roomPrice) {
        this.roomPrice = roomPrice;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public long getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(long paidAt) {
        this.paidAt = paidAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
