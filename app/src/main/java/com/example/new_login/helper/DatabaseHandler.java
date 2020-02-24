package com.example.new_login.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

public class DatabaseHandler extends SQLiteOpenHelper {

//    all static variables
//    database version
    private static final int DATABASE_VERSION = 1;

//    database name
    private static final String DATABASE_NAME = "id12539836_android_login";
//    login table name
    private static final String TABLE_LOGIN =  "login";

//    login table columns names
    private static final String KEY_ID = "id";
    private static final String KEY_UID = "uid";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_CREATED_AT = "created_at";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_UID + " TEXT,"
            + KEY_NAME + " TEXT,"
            + KEY_EMAIL + " TEXT UNIQUE,"
            + KEY_CREATED_AT + " TEXT" + ")";

//    creating table
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_LOGIN_TABLE);
    }

//    upgrading database

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
//        create table again
        onCreate(db);
    }

//    menampilkan detail user di database
    public void addUser(String uid, String name, String email, String created_at){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_UID, uid);
        values.put(KEY_NAME, name);
        values.put(KEY_EMAIL, email);
        values.put(KEY_CREATED_AT, created_at);

//        insert Row
        db.insert(TABLE_LOGIN, null ,values);
        db.close();//close database connection
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String,String> user = new HashMap<String,String>();
        String selectQuery = "SELECT  * FROM " + TABLE_LOGIN;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            user.put("uid", cursor.getString(1));
            user.put("name", cursor.getString(2));
            user.put("email", cursor.getString(3));
            user.put("created_at", cursor.getString(4));
        }
        cursor.close();
        db.close();
        // return user
        return user;
    }

//
    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + TABLE_LOGIN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        // return row count
        return rowCount;
    }

    public void resetTables(){
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_LOGIN, null, null);
        db.close();
    }


}
