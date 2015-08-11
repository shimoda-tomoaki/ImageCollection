package com.example.shimoda_tomoaki.helloworld;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {
    final static String TABLE_FOLDER = "folder";
    final static String TABLE_IMAGE = "image";
    final static String TABLE_URL = "url";

    final static String COLUMN_ID = "_id";
    final static String COLUMN_FOLDER_NAME = "folderName";
    final static String COLUMN_FOLDER_ID = "folderId";
    final static String COLUMN_PASSWORD = "password";
    final static String COLUMN_IS_LOCKED = "isLocked";
    final static String COLUMN_IS_UNPUBLISHED = "isUnpublished";
    final static String COLUMN_IMAGE = "image";
    final static String COLUMN_URL = "url";
    final static String COLUMN_CREATED_DATE = "createdDate";

    SQLiteDatabase mDB;

    public static String[] getFolderColumnNames() {
        return new String[]{COLUMN_ID, COLUMN_FOLDER_NAME, COLUMN_PASSWORD, COLUMN_IS_LOCKED, COLUMN_IS_UNPUBLISHED};
    }

    public static String[] getImageColumnNames() {
        return new String[]{COLUMN_ID, COLUMN_IMAGE, COLUMN_FOLDER_ID, COLUMN_CREATED_DATE};
    }

    public static String[] getUrlColumnNames() {
        return new String[]{COLUMN_ID, COLUMN_URL, COLUMN_FOLDER_ID, COLUMN_CREATED_DATE};
    }

    public DBManager(Context context) {
        mDB = new MySQLiteOpenHelper(context).getWritableDatabase();
    }

    public SQLiteDatabase getDB() {
        return mDB;
    }

    public boolean isExistCategory(int folderId, String folderName) {
        Cursor cursor = mDB.query(TABLE_FOLDER, null, DBManager.COLUMN_ID + " != ? AND " + DBManager.COLUMN_FOLDER_NAME + " = ?", new String[]{"" + folderId, folderName}, null, null, null);
        return cursor.moveToFirst();
    }

    public boolean isExistPasswordInUnPublic(int folderId, String password) {
        Cursor cursor = mDB.query(DBManager.TABLE_FOLDER, null, DBManager.COLUMN_ID + " != ? AND " + DBManager.COLUMN_PASSWORD + " = ?", new String[]{"" + folderId, password}, null, null, null);

        while (cursor.moveToNext()) {
            if (cursor.getInt(cursor.getColumnIndex(DBManager.COLUMN_IS_UNPUBLISHED)) == 1) return true;
        }

        return false;
    }

    public static void removeCategory(Context context, int folderId) {
        SQLiteDatabase db = new MySQLiteOpenHelper(context).getWritableDatabase();

        db.delete(DBManager.TABLE_FOLDER, DBManager.COLUMN_ID + " = ?", new String[]{"" + folderId});
        db.delete(DBManager.TABLE_URL, DBManager.COLUMN_FOLDER_ID + " = ?", new String[]{"" + folderId});
        db.delete(DBManager.TABLE_IMAGE, DBManager.COLUMN_FOLDER_ID + " = ?", new String[]{"" + folderId});

        db.close();
    }
}
