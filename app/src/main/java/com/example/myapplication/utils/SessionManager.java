package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "MotelAppSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_ROLE = "role";
    private static final String KEY_ROOM_ID = "roomId";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_PHONE = "phone";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(long userId, String role, Long roomId, String fullName, String phone) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_ROLE, role);
        editor.putLong(KEY_ROOM_ID, roomId != null ? roomId : -1L);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_PHONE, phone);
        editor.apply();
    }

    public void updateRoomId(long roomId) {
        editor.putLong(KEY_ROOM_ID, roomId);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public long getUserId() {
        return pref.getLong(KEY_USER_ID, -1L);
    }

    public String getRole() {
        return pref.getString(KEY_ROLE, "");
    }

    public long getRoomId() {
        return pref.getLong(KEY_ROOM_ID, -1L);
    }

    public String getFullName() {
        return pref.getString(KEY_FULL_NAME, "");
    }

    public String getPhone() {
        return pref.getString(KEY_PHONE, "");
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
