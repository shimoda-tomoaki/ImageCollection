package com.example.shimoda_tomoaki.helloworld;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
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
    private static final String ARG_CATEGORY_ID = "categoryId";

    private View mRootView;
    private int mCategoryId;

    public OnFragmentInteractionListener mListener;

    public static WebFragment newInstance(int categoryId) {
        WebFragment fragment = new WebFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY_ID, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    public WebFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCategoryId = getArguments().getInt(ARG_CATEGORY_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_web, container, false);

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
                    new GetBitmap().execute(hr.getExtra());
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

    public interface OnFragmentInteractionListener {}

    public boolean goBack() {
        WebView webView = (WebView) mRootView.findViewById(R.id.webView);
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }

    public class GetBitmap extends AsyncTask<String, Integer, Bitmap> {
        private URL mUrl;

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
                db.close();
            }
        }
    }

    public class JavaScriptInterface {
        Context mContext;

        JavaScriptInterface(Context c) {
            mContext = c;
        }
    }
}
