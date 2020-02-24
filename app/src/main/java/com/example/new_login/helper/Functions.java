package com.example.new_login.helper;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

public class Functions {
    public static String MAIN_URL = "https://zainbananaserveronly.000webhostapp.com/";
    public static String LOGIN_URL = MAIN_URL + "login.php";
    public static String REGISTER_URL = MAIN_URL + "register.php";
    public static String OTP_VERIFY_URL = MAIN_URL + "verification.php";
    public static String RESET_PASS_URL = MAIN_URL + "reset-password.php";

//    fungsi untuk logout
    public boolean logoutUser (Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        db.resetTables();
        return true;
    }
//email address validation
    public static boolean isValidEmailAddress(String email){
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }
}
