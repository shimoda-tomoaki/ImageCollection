package com.example.shimoda_tomoaki.helloworld;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.support.v4.app.Fragment;

public class WebFragment extends Fragment {
    private static final String ARG_FOLDER_ID = "folderId";

    private View mRootView;
    private int mFolderId;

    public OnFragmentInteractionListener mListener;

    public static WebFragment newInstance(int folderId) {
        WebFragment fragment = new WebFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FOLDER_ID, folderId);
        fragment.setArguments(args);
        return fragment;
    }

    public WebFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFolderId = getArguments().getInt(ARG_FOLDER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_web, container, false);

        WebView webView = (WebView) mRootView.findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setOnLongClickListener(view -> {
            WebView webView1 = (WebView) view;
            WebView.HitTestResult hr = webView1.getHitTestResult();

            if (hr.getType() == WebView.HitTestResult.IMAGE_TYPE
                    || hr.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                new GetBitmap(getActivity(), mFolderId).execute(hr.getExtra());
            }

            return true;
        });
        webView.loadUrl("http://google.com");
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
}
