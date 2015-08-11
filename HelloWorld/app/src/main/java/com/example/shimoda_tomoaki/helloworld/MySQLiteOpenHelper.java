package com.example.shimoda_tomoaki.helloworld;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    final private static int DATABASE_VERSION = 1;

    public MySQLiteOpenHelper(Context context) {
        super(context, "Sample.db", null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + DBManager.TABLE_FOLDER);
        db.execSQL("CREATE TABLE " + DBManager.TABLE_FOLDER + " (" +
                DBManager.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                DBManager.COLUMN_FOLDER_NAME + " TEXT NOT NULL, " +
                DBManager.COLUMN_PASSWORD + " TEXT, " +
                DBManager.COLUMN_IS_LOCKED + " INTEGER DEFAULT 0, " +
                DBManager.COLUMN_IS_UNPUBLISHED + " INTEGER DEFAULT 0)");

        db.execSQL("DROP TABLE IF EXISTS " + DBManager.TABLE_URL);
        db.execSQL("CREATE TABLE " + DBManager.TABLE_URL + " (" +
                DBManager.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                DBManager.COLUMN_FOLDER_ID + " INTEGER NOT NULL, " +
                DBManager.COLUMN_URL + " TEXT NOT NULL, " +
                DBManager.COLUMN_CREATED_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("DROP TABLE IF EXISTS " + DBManager.TABLE_IMAGE);
        db.execSQL("CREATE TABLE " + DBManager.TABLE_IMAGE + " (" +
                DBManager.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                DBManager.COLUMN_FOLDER_ID + " INTEGER NOT NULL, " +
                DBManager.COLUMN_IMAGE + " BLOB NOT NULL, " +
                DBManager.COLUMN_CREATED_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}