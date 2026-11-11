package com.example.myapplication.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rooms")
public class Room {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String roomNumber; // e.g. "101"
    private double basePrice;
    private double elecUnitPrice;
    private double waterUnitPrice;
    private double wifiFee;
    private double garbageFee;
    private double bikeUnitPrice;
    private int currentBikeCount;
    private String status; // "TRONG" or "DANG_THUE"
    private String qrJoinToken; // Unique token for QR room joining, e.g. "JOIN_P101_2026"

    public Room() {
    }

    public Room(String roomNumber, double basePrice, double elecUnitPrice, double waterUnitPrice,
                double wifiFee, double garbageFee, double bikeUnitPrice, int currentBikeCount,
                String status, String qrJoinToken) {
        this.roomNumber = roomNumber;
        this.basePrice = basePrice;
        this.elecUnitPrice = elecUnitPrice;
        this.waterUnitPrice = waterUnitPrice;
        this.wifiFee = wifiFee;
        this.garbageFee = garbageFee;
        this.bikeUnitPrice = bikeUnitPrice;
        this.currentBikeCount = currentBikeCount;
        this.status = status;
        this.qrJoinToken = qrJoinToken;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getElecUnitPrice() {
        return elecUnitPrice;
    }

    public void setElecUnitPrice(double elecUnitPrice) {
        this.elecUnitPrice = elecUnitPrice;
    }

    public double getWaterUnitPrice() {
        return waterUnitPrice;
    }

    public void setWaterUnitPrice(double waterUnitPrice) {
        this.waterUnitPrice = waterUnitPrice;
    }

    public double getWifiFee() {
        return wifiFee;
    }

    public void setWifiFee(double wifiFee) {
        this.wifiFee = wifiFee;
    }

    public double getGarbageFee() {
        return garbageFee;
    }

    public void setGarbageFee(double garbageFee) {
        this.garbageFee = garbageFee;
    }

    public double getBikeUnitPrice() {
        return bikeUnitPrice;
    }

    public void setBikeUnitPrice(double bikeUnitPrice) {
        this.bikeUnitPrice = bikeUnitPrice;
    }

    public int getCurrentBikeCount() {
        return currentBikeCount;
    }

    public void setCurrentBikeCount(int currentBikeCount) {
        this.currentBikeCount = currentBikeCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQrJoinToken() {
        return qrJoinToken;
    }

    public void setQrJoinToken(String qrJoinToken) {
        this.qrJoinToken = qrJoinToken;
    }
}
