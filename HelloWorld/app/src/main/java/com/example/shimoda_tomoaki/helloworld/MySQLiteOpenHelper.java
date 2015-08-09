package com.example.shimoda_tomoaki.helloworld;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    final private static int DATABASE_VERSION = 1;
    private static Context sContext;

    public MySQLiteOpenHelper(Context context) {
        super(context, "Sample.db", null, DATABASE_VERSION);
        sContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + sContext.getString(R.string.table_category));
        db.execSQL("CREATE TABLE " + sContext.getString(R.string.table_category)
                + " (" + sContext.getString(R.string.column_id) + " INTEGER PRIMARY KEY, category TEXT NOT NULL, password TEXT, isLocked INTEGER DEFAULT 0, isUnpublished INTEGER DEFAULT 0)");
        db.execSQL("DROP TABLE IF EXISTS " + sContext.getString(R.string.table_url));
        db.execSQL("CREATE TABLE " + sContext.getString(R.string.table_url)
                + " (" + sContext.getString(R.string.column_id) + " INTEGER PRIMARY KEY, categoryId INTEGER NOT NULL, url TEXT NOT NULL, created_date DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("DROP TABLE IF EXISTS " + sContext.getString(R.string.table_image));
        db.execSQL("CREATE TABLE " + sContext.getString(R.string.table_image)
                + " (" + sContext.getString(R.string.column_id) + " INTEGER PRIMARY KEY, categoryId INTEGER NOT NULL, image BLOB NOT NULL, created_date DATETIME DEFAULT CURRENT_TIMESTAMP)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}