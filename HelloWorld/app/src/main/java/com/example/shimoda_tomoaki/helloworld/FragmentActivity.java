package com.example.shimoda_tomoaki.helloworld;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;


public class FragmentActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        WebFragment.OnFragmentInteractionListener,
        ImageListFragment.OnFragmentInteractionListener,
        SettingFragment.OnFragmentInteractionListener {
    static final String ARG_FOLDER_ID = "folderId";
    static final String ARG_FOLDER_NAME = "folderName";

    private String mFolderName;
    private int mFolderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mFolderId = intent.getIntExtra(ARG_FOLDER_ID, -1);
        mFolderName = intent.getStringExtra(ARG_FOLDER_NAME);

        setContentView(R.layout.activity_fragment);

        NavigationDrawerFragment navigationDrawerFragment =
                (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        getSupportActionBar().setTitle(mFolderName + "：画像一覧");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, ImageListFragment.newInstance(mFolderId))
                .commit();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch(position) {
            case 0:
                getSupportActionBar().setTitle(mFolderName + "：画像一覧");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, ImageListFragment.newInstance(mFolderId))
                        .addToBackStack(null)
                        .commit();
                break;
            case 1:
                getSupportActionBar().setTitle(mFolderName + "：ブラウザ");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, WebFragment.newInstance(mFolderId))
                        .addToBackStack(null)
                        .commit();
                break;
            case 2:
                getSupportActionBar().setTitle(mFolderName + "：設定");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, SettingFragment.newInstance(mFolderId, mFolderName))
                        .addToBackStack(null)
                        .commit();
                break;
            case 3:
                startActivity(new Intent(getApplicationContext(),TopActivity.class));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment instanceof WebFragment && ((WebFragment) fragment).goBack()) return;
        else if (fragment instanceof ImageListFragment && ((ImageListFragment) fragment).cancelPreviewMode()) return;
        finish();
    }
}
