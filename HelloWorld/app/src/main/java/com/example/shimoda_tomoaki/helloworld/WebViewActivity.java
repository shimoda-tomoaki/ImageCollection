package com.example.shimoda_tomoaki.helloworld;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.shimoda_tomoaki.helloworld.R.layout.activity_web_view;


public class WebViewActivity extends ActionBarActivity {
    private int mCategoryId;
    private String mCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_web_view);

        Intent intent = getIntent();
        mCategory = intent.getStringExtra("category");
        mCategoryId = intent.getIntExtra("categoryId", -1);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("ブラウザ");
        actionBar.setSubtitle("");

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new JavaScriptInterface(getApplicationContext()), "Android");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                WebView webView = (WebView) view;
                WebView.HitTestResult hr = webView.getHitTestResult();

                if (hr.getType() == WebView.HitTestResult.IMAGE_TYPE
                        || hr.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    Log.d("myTag", "imageUrl:" + hr.getExtra());

                    try {
                        GetBitmap getBitmap = new GetBitmap(new URL(hr.getExtra()));
                        getBitmap.execute();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }

                return true;
            }
        });
        webView.loadUrl("http://google.com");
        webView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        WebView  myWebView = (WebView)findViewById(R.id.webView);
        // 端末のBACKキーで一つ前のページヘ戻る
        if(keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {
            myWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode,  event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class GetBitmap extends AsyncTask<String, Integer, Bitmap> {
        private static final String TAG = "myTag";
        private URL mUrl;

        public GetBitmap (URL url) {
            super();
            mUrl = url;
        }

        @Override
        protected void onPreExecute() {
            //Log.d(TAG, "下準備");
        }

        @Override
        protected Bitmap doInBackground(String... inputParams) {
            //Log.d(TAG, "DownloadTask.doInBackground(" + mUrl + ")");

            Bitmap bitmap = null;
            try {
                InputStream input = (InputStream)mUrl.getContent();
                Drawable d = Drawable.createFromStream(input, "src");
                bitmap = ((BitmapDrawable)d).getBitmap();
            } catch (IOException e) {
                e.printStackTrace();
            }
            publishProgress(100);

            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            //Log.d(TAG, "DownloadTask.onProgressUpdate(" + progress[0] + ")");
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            //Log.d(TAG, "DownloadTask.onPostExecute");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            result.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bitmapData = stream.toByteArray();

            ContentValues value = new ContentValues();
            value.put("categoryId", mCategoryId);
            value.put("image", bitmapData);

            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("data/data/" + getPackageName() + "/Sample.db", null);
            db.beginTransaction();
            try {
                db.insert("image", null, value);
                db.setTransactionSuccessful();
                Toast.makeText(getApplicationContext(), "image get!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "DownloadTask.onCancelled");
        }
    }

    public class JavaScriptInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        JavaScriptInterface(Context c) {
            mContext = c;
        }
    }
}