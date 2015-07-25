package com.example.shimoda_tomoaki.helloworld;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;


public class FragmentActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        WebFragment.OnFragmentInteractionListener,
        ImageListFragment.OnFragmentInteractionListener,
        SettingFragment.OnFragmentInteractionListener {
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private String mCategory;
    private int mCategoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        Intent intent = getIntent();
        mCategoryId = intent.getIntExtra("categoryId", -1);
        mCategory = intent.getStringExtra("category");

        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        getSupportActionBar().setTitle(mCategory + "：画像一覧");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, ImageListFragment.newInstance(mCategoryId), "web")
                .commit();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch(position) {
            case 0:
                getSupportActionBar().setTitle(mCategory + "：画像一覧");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, ImageListFragment.newInstance(mCategoryId), "web")
                        .addToBackStack(null)
                        .commit();
                break;
            case 1:
                getSupportActionBar().setTitle(mCategory + "：ブラウザ");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, WebFragment.newInstance(mCategoryId, mCategory), "web")
                        .addToBackStack(null)
                        .commit();
                break;
            case 2:
                getSupportActionBar().setTitle(mCategory + "：設定");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, SettingFragment.newInstance(mCategoryId, mCategory), "web")
                        .addToBackStack(null)
                        .commit();
                break;
            case 3:
                startActivity(new Intent(getApplicationContext(),TopActivity.class));
                break;
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                setTitle("ブラウザ");
                break;
            case 1:
                setTitle("画像一覧");
                break;
            case 2:
                setTitle("設定");
                break;
            default:
                break;
        }
    }



    public void restoreActionBar() {
        Log.d("tag", "restoreActionBar");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
    }


    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d("tag", "onCreateView");
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((FragmentActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    @Override public void onWebFragmentInteraction(Uri uri) {}
    @Override public void onImageListFragmentInteraction(Uri uri) {};
    @Override public void onSettingFragmentInteraction(Uri uri) {};

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if(fragment instanceof WebFragment && ((WebFragment) fragment).goBack()) {
            return;
        }
        finish();
    }
}
