package com.example.shimoda_tomoaki.helloworld;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.support.v4.app.Fragment;

public class WebFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CATEGORY_ID = "categoryId";
    private static final String ARG_CATEGORY = "category";

    private View mRootView;
    private int mCategoryId;
    private String mCategory;

    private OnFragmentInteractionListener mListener;

    public static WebFragment newInstance(int categoryId, String category) {
        WebFragment fragment = new WebFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    public WebFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCategoryId = getArguments().getInt(ARG_CATEGORY_ID);
            mCategory = getArguments().getString(ARG_CATEGORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("tag", "point2");
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_web, container, false);

        Log.d("tag", "point1");
        WebView webView = (WebView) mRootView.findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new JavaScriptInterface(getActivity()), "Android");
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
        return mRootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onWebFragmentInteraction(Uri uri);
    }

    public boolean goBack() {
        WebView webView = (WebView) mRootView.findViewById(R.id.webView);
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        WebView  myWebView = (WebView) mRootView.findViewById(R.id.webView);
//        // 端末のBACKキーで一つ前のページヘ戻る
//        if(keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {
//            myWebView.goBack();
//            return true;
//        }
//        return onKeyDown(keyCode, event);
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        switch (id) {
//            case R.id.action_settings:
//                return true;
//            case android.R.id.home:
//                finish();
//                return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

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

            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("data/data/" + getActivity().getPackageName() + "/Sample.db", null);
            db.beginTransaction();
            try {
                db.insert("image", null, value);
                db.setTransactionSuccessful();
                Toast.makeText(getActivity(), "image get!", Toast.LENGTH_SHORT).show();
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
