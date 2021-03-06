package com.example.shimoda_tomoaki.helloworld;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class GetBitmap extends AsyncTask<String, Integer, Bitmap> {
    private Context mContext;
    private int mFolderId;
    private URL mUrl;

    public GetBitmap(Context context, int folderId) {
        mFolderId = folderId;
        mContext = context;
    }

    @Override
    protected Bitmap doInBackground(String... inputParams) {
        try {
            mUrl = new URL(inputParams[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = null;

        try {
            InputStream input = (InputStream)mUrl.getContent();
            Drawable d = Drawable.createFromStream(input, "src");
            bitmap = ((BitmapDrawable)d).getBitmap();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapData = stream.toByteArray();

        ContentValues value = new ContentValues();
        value.put(DBManager.COLUMN_FOLDER_ID, mFolderId);
        value.put(DBManager.COLUMN_IMAGE, bitmapData);

        SQLiteDatabase db = new MySQLiteOpenHelper(mContext).getWritableDatabase();
        db.beginTransaction();
        try {
            db.insert("image", null, value);
            db.setTransactionSuccessful();
            Toast.makeText(mContext, "image get!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }
}