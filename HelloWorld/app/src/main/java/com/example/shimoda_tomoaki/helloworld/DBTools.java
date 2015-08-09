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

    public static void updateDatabaseFrom1(Context context) {
        class OldCategoryData {
            private String mCategory;
            private String mPassword;
            private boolean mIsLocked;
            private boolean mIsUnpublished;

            public OldCategoryData(String category, String password, boolean isLocked, boolean isUnpublished) {
                mCategory = category;
                mPassword = password;
                mIsLocked = isLocked;
                mIsUnpublished = isUnpublished;
            }

            public String getCategory() { return mCategory; }
            public String getPassword() { return mPassword; }
            public boolean getIsLocked() { return mIsLocked; }
            public boolean getIsUnpublished() { return mIsUnpublished; }
        }

        class OldUrlData {
            private int mCategoryId;
            private String mUrl;
            private Timestamp mCreatedDate;


            public OldUrlData(int categoryId, String url, Timestamp createdDate) {
                mCategoryId = categoryId;
                mUrl = url;
                mCreatedDate = createdDate;
            }

            public int getCategoryId() { return mCategoryId; }
            public String getUrl() { return mUrl; }
            public Timestamp getCreateDate() { return mCreatedDate; }
        }

        class OldImageData {
            private int mCategoryId;
            private byte[] mBlob;
            private Timestamp mCreatedDate;

            public OldImageData(int categoryId, byte[] blob, Timestamp createdDate) {
                mCategoryId = categoryId;
                mBlob = blob;
                mCreatedDate = createdDate;
            }

            public int getCategoryId() { return mCategoryId; }
            public byte[] getblob() { return mBlob; }
            public Timestamp getCreateDate() { return mCreatedDate; }
        }

        ArrayList<OldCategoryData> oldCategoryDataList = new ArrayList<>();
        ArrayList<OldUrlData> oldUrlDataList = new ArrayList<>();
        ArrayList<OldImageData> oldImageDataList = new ArrayList<>();
        Cursor cursor;

        SQLiteDatabase oldDB = SQLiteDatabase.openOrCreateDatabase("data/data/" + context.getPackageName() + "/Sample.db", null);

        cursor = oldDB.query("category", new String[]{"category", "password", "isLocked", "isUnpublished"}, null, null, null, null, "id");
        while(cursor.moveToNext()) {
            oldCategoryDataList.add(new OldCategoryData(
                    cursor.getString(cursor.getColumnIndex("category")),
                    cursor.getString(cursor.getColumnIndex("password")),
                    cursor.getInt(cursor.getColumnIndex("isLocked")) == 1,
                    cursor.getInt(cursor.getColumnIndex("isUnpublished")) == 1
            ));
        }

        cursor = oldDB.query("url", new String[]{"categoryId", "url", "createdDate"}, null, null, null, null, null, "id");
        while(cursor.moveToNext()) {
            oldUrlDataList.add(new OldUrlData(
                    cursor.getInt(cursor.getColumnIndex("categoryId")),
                    cursor.getString(cursor.getColumnIndex("url")),
                    Timestamp.valueOf(cursor.getString(cursor.getColumnIndex("createdDate")))
            ));
        }

        cursor = oldDB.query("image", new String[]{"categoryId", "image", "createdDate"}, null, null, null, null, null, "id");
        while(cursor.moveToNext()) {
            oldImageDataList.add(new OldImageData(
                    cursor.getInt(cursor.getColumnIndex("categoryId")),
                    cursor.getBlob(cursor.getColumnIndex("image")),
                    Timestamp.valueOf(cursor.getString(cursor.getColumnIndex("createdDate")))
            ));
        }

        cursor = oldDB.query("url", new String[]{"categoryId", "url", "createdDate"}, null, null, null, null, null, "id");
        while(cursor.moveToNext()) {
            oldUrlDataList.add(new OldUrlData(
                    cursor.getInt(cursor.getColumnIndex("categoryId")),
                    cursor.getString(cursor.getColumnIndex("url")),
                    Timestamp.valueOf(cursor.getString(cursor.getColumnIndex("createdDate")))
            ));
        }

        SQLiteDatabase newDB = new MySQLiteOpenHelper(context).getWritableDatabase();

        for (OldCategoryData categoryData : oldCategoryDataList) {
            ContentValues values = new ContentValues();
            values.put("category", categoryData.getCategory());
            values.put("password", categoryData.getPassword());
            values.put("isLocked", categoryData.getIsLocked());
            values.put("isUnpublished", categoryData.getIsUnpublished());
            newDB.insert("category", null, values);
        }

        for (OldImageData imageData : oldImageDataList) {
            ContentValues values = new ContentValues();
            values.put("categoryId", imageData.getCategoryId());
            values.put("image", imageData.getblob());
            values.put("createdDate", imageData.getCreateDate().toString());
            newDB.insert("image", null, values);
        }

        for (OldUrlData urlData : oldUrlDataList) {
            ContentValues values = new ContentValues();
            values.put("categoryId", urlData.getCategoryId());
            values.put("url", urlData.getUrl());
            values.put("createdDate", urlData.getCreateDate().toString());
            newDB.insert("url", null, values);
        }

        oldDB.execSQL("DROP TABLE IF EXISTS " + context.getString(R.string.table_category));
        oldDB.execSQL("DROP TABLE IF EXISTS " + context.getString(R.string.table_url));
        oldDB.execSQL("DROP TABLE IF EXISTS " + context.getString(R.string.table_image));
    }
}
