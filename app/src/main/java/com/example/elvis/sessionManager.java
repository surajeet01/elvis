package com.example.elvis;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class sessionManager {

    SharedPreferences userSession;
    SharedPreferences.Editor editor;
    Context context;

    private static final String IS_LOGIN = "No";
    public  static final String NEW_USER = "Yes";

    public  static final String KEY_NAME = "fullName";
    public  static final String KEY_MOBILE = "mobile";
    public  static final String KEY_SEX = "sex";
    public  static final String KEY_DOB = "dob";
    public  static final String KEY_EMAIL = "email";
    public  static final String KEY_UID = "uid";


    public sessionManager(Context _context) {
        context = _context;
        userSession = context.getSharedPreferences("userLoginSession", context.MODE_PRIVATE);
        editor = userSession.edit();
    }

    public void createInitLoginSessionWithMobile(String uid, String mobile) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putBoolean(NEW_USER, true);

        editor.putString(KEY_UID, uid);
        editor.putString(KEY_MOBILE, mobile);

        editor.commit();
    }

    public void createInitLoginSessionWithEmail(String uid, String email) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putBoolean(NEW_USER, true);

        editor.putString(KEY_UID, uid);
        editor.putString(KEY_EMAIL, email);

        editor.commit();
    }

    public void createLoginSession(String uid, String mobile, String name, String sex, String dob, String email) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putBoolean(NEW_USER, false);

        editor.putString(KEY_UID, uid);
        editor.putString(KEY_MOBILE, mobile);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_SEX, sex);
        editor.putString(KEY_DOB, dob);
        editor.putString(KEY_EMAIL, email);

        editor.commit();
    }

    public HashMap<String, String> getUserDetailsFromSession() {
        HashMap<String, String> userData = new HashMap<String, String>();

        userData.put(KEY_UID, userSession.getString(KEY_UID, null));

        userData.put(KEY_MOBILE, userSession.getString(KEY_MOBILE, null));
        userData.put(KEY_EMAIL, userSession.getString(KEY_EMAIL, null));

        if(!userSession.getBoolean(NEW_USER, true)) {
            userData.put(KEY_NAME, userSession.getString(KEY_NAME, null));
            userData.put(KEY_SEX, userSession.getString(KEY_SEX, null));
            userData.put(KEY_DOB, userSession.getString(KEY_DOB, null));

            userData.put(KEY_EMAIL, userSession.getString(KEY_EMAIL, null));
            userData.put(KEY_MOBILE, userSession.getString(KEY_MOBILE, null));
        }

        return userData;
    }

    public boolean isLoggedIn() {
        if(userSession.getBoolean(IS_LOGIN, false))
            return true;
        return false;
    }

    public boolean isNewUser() {
        if(userSession.getBoolean(NEW_USER, false))
            return true;
        return false;
    }

    public void destroyLoginSession() {
        editor.clear();
        editor.commit();
    }
}
