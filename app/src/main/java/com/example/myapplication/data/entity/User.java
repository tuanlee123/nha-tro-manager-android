package com.example.myapplication.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = {"phone"}, unique = true)})
public class User {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String fullName;
    private String phone;
    private String password;
    private String role; // "OWNER" or "TENANT"
    private String idCard; // CCCD for tenant
    private String boardingHouseName; // Name of boarding house for owner
    private String bankBin; // Bank BIN code (e.g. "970422" for MB Bank)
    private String bankAccountNo; // Owner bank account number
    private String bankAccountName; // Owner bank account holder name
    private Long roomId; // Nullable, room ID tenant is currently staying in

    public User() {
    }

    public User(String fullName, String phone, String password, String role, String idCard,
                String boardingHouseName, String bankBin, String bankAccountNo, String bankAccountName, Long roomId) {
        this.fullName = fullName;
        this.phone = phone;
        this.password = password;
        this.role = role;
        this.idCard = idCard;
        this.boardingHouseName = boardingHouseName;
        this.bankBin = bankBin;
        this.bankAccountNo = bankAccountNo;
        this.bankAccountName = bankAccountName;
        this.roomId = roomId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getBoardingHouseName() {
        return boardingHouseName;
    }

    public void setBoardingHouseName(String boardingHouseName) {
        this.boardingHouseName = boardingHouseName;
    }

    public String getBankBin() {
        return bankBin;
    }

    public void setBankBin(String bankBin) {
        this.bankBin = bankBin;
    }

    public String getBankAccountNo() {
        return bankAccountNo;
    }

    public void setBankAccountNo(String bankAccountNo) {
        this.bankAccountNo = bankAccountNo;
    }

    public String getBankAccountName() {
        return bankAccountName;
    }

    public void setBankAccountName(String bankAccountName) {
        this.bankAccountName = bankAccountName;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
}
