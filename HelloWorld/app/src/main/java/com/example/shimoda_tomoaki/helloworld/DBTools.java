package com.example.shimoda_tomoaki.helloworld;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by shimoda-tomoaki on 2015/07/17.
 */
public class DBTools {
    SQLiteDatabase mDb;

    public DBTools(Context context) {
        mDb = SQLiteDatabase.openOrCreateDatabase("data/data/" + context.getPackageName() + "/Sample.db", null);
    }

    public static void makeTable(Context context) {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("data/data/" + context.getPackageName() + "/Sample.db", null);

        db.execSQL("DROP TABLE IF EXISTS category");
        db.execSQL("CREATE TABLE category "
                + "(_id INTEGER PRIMARY KEY, category TEXT NOT NULL, password TEXT, isLocked INTEGER DEFAULT 0, isUnpublished INTEGER DEFAULT 0)");
        db.execSQL("DROP TABLE IF EXISTS url");
        db.execSQL("CREATE TABLE url "
                + "(_id INTEGER PRIMARY KEY, categoryId INTEGER NOT NULL, url TEXT NOT NULL, created_date DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("DROP TABLE IF EXISTS image");
        db.execSQL("CREATE TABLE image "
                + "(_id INTEGER PRIMARY KEY, categoryId INTEGER NOT NULL, image BLOB NOT NULL, created_date DATETIME DEFAULT CURRENT_TIMESTAMP)");
    }

    public boolean isExistCategory(int categoryId, String category) {
        Cursor cursor = mDb.query("category", null, "_id != ? AND category = ?", new String[]{"" + categoryId, category}, null, null, null);
        return cursor.moveToFirst();
    }

    public boolean isExistPasswordInUnPublic(int categoryId, String password) {
        Cursor cursor = mDb.query("category", null, "_id != ? AND password = ?", new String[]{"" + categoryId, password}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getInt(cursor.getColumnIndex("isUnpublished")) == 1) return true;
            } while (cursor.moveToNext());
        }
        return false;
    }

    public static SQLiteDatabase getDatabase(Context context) {
        return SQLiteDatabase.openOrCreateDatabase("data/data/" + context.getPackageName() + "/Sample.db", null);
    }

    public static void removeCategory(Context context, int categoryId) {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("data/data/" + context.getPackageName() + "/Sample.db", null);

        db.delete("category", "_id = ?", new String[]{"" + categoryId});
        db.delete("url", "categoryId = ?", new String[]{"" + categoryId});
        db.delete("image", "categoryId = ?", new String[]{"" + categoryId});
    }
}
