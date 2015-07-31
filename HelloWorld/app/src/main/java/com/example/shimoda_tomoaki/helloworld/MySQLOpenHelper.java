package com.example.shimoda_tomoaki.helloworld;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by shimoda-tomoaki on 2015/07/27.
 */
public class MySQLOpenHelper  extends SQLiteOpenHelper {
    public MySQLOpenHelper(Context c) {
        super(c, "dbname", null, 1);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table -省略-");
    }

    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        //実装例
        //現在のレコードを取得して、一旦メモリへ退避。
        //テーブルの削除
        //新しくテーブルを作り直して、
        //メモリへ退避させたレコードを挿入する　etc
        }
}
