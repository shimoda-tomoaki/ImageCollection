package com.example.shimoda_tomoaki.helloworld;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Timestamp;
import java.util.ArrayList;

public class DBTools {
    SQLiteDatabase mDb;

    public DBTools(Context context) {
        mDb = new MySQLiteOpenHelper(context).getWritableDatabase();
    }


    public boolean isExistCategory(int categoryId, String category) {
        Cursor cursor = mDb.query("category", null, "id != ? AND category = ?", new String[]{"" + categoryId, category}, null, null, null);
        return cursor.moveToFirst();
    }

    public boolean isExistPasswordInUnPublic(int categoryId, String password) {
        Cursor cursor = mDb.query("category", null, "id != ? AND password = ?", new String[]{"" + categoryId, password}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getInt(cursor.getColumnIndex("isUnpublished")) == 1) return true;
            } while (cursor.moveToNext());
        }
        return false;
    }

    public static SQLiteDatabase getDatabase(Context context) {
        return new MySQLiteOpenHelper(context).getWritableDatabase();
    }

    public static void removeCategory(Context context, int categoryId) {
        SQLiteDatabase db = new MySQLiteOpenHelper(context).getWritableDatabase();

        db.delete("category", "id = ?", new String[]{"" + categoryId});
        db.delete("url", "categoryId = ?", new String[]{"" + categoryId});
        db.delete("image", "categoryId = ?", new String[]{"" + categoryId});
    }
}
