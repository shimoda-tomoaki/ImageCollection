package com.example.shimoda_tomoaki.helloworld;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class TopActivity extends ActionBarActivity {
    private ListView mListView;
    private ArrayList<String> mDataList;
    private ArrayAdapter<String> mAdapter;
    private int mCategoryId;
    private String mCategory;
    private String mPassword;
    private String mSettingPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);

        SharedPreferences preference = getSharedPreferences("Preference Name", MODE_PRIVATE);
        if (!preference.getBoolean("Launched", false)) {
            preference.edit().putBoolean("Launched", true).apply();
            DBTools.makeTable(this);
        }

        mSettingPassword = preference.getString("SettingPassword", "");
        if (mSettingPassword.isEmpty()) {
            DialogFragment newFragment = new InputPasswordCustomDialog();
            newFragment.show(getFragmentManager(), InputPasswordCustomDialog.SITUATION_INPUT_SETTING_PASSWORD);
        }

        getSupportActionBar().setTitle("画像収集アプリ");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings1) {
            DialogFragment newFragment = new InputCategoryDialog();
            newFragment.show(getFragmentManager(), null);
            return true;
        } else if (id == R.id.action_settings2) {
            DialogFragment newFragment = new InputPasswordCustomDialog();
            newFragment.show(getFragmentManager(), InputPasswordCustomDialog.SITUATION_SELECT_CATEGORY_LIST);
            return true;
        } else if (id == R.id.action_settings3) {
            DialogFragment newFragment = new ChangePasswordDialog();
            newFragment.show(getFragmentManager(), null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        categoryListSet();
    }

    public void selectLockedActivity(String inputPassword) {
        if (inputPassword.equals(mPassword)) {
            Intent intent = new Intent(getApplicationContext(), FragmentActivity.class);
            intent.putExtra("category", mCategory);
            intent.putExtra("categoryId", mCategoryId);
            startActivity(intent);
        } else {
            Toast.makeText(this, "パスワードが間違っています", Toast.LENGTH_SHORT).show();
        }
    }

    public void selectUnpublishedActivity(String inputPassword) {
        SQLiteDatabase db = DBTools.getDatabase(this);
        Cursor cursor = db.query("category", new String[]{"_id", "category"}, "password = ?", new String[]{inputPassword}, null, null, null);

        if(cursor.moveToFirst()) {
            Intent intent = new Intent(getApplicationContext(), FragmentActivity.class);
            intent.putExtra("category", cursor.getString(cursor.getColumnIndex("category")));
            intent.putExtra("categoryId", cursor.getInt(cursor.getColumnIndex("_id")));
            startActivity(intent);
        } else {
            Toast.makeText(this, "入力したパスワードの非表示フォルダは存在しません", Toast.LENGTH_SHORT).show();
        }
    }

    public void selectCategoryList(String inputPassword) {
        SharedPreferences preference = getSharedPreferences("Preference Name", MODE_PRIVATE);

        if(inputPassword.equals(preference.getString("SettingPassword", ""))) {
            startActivity(new Intent(getApplicationContext(), CategoryListActivity.class));
        } else {
            Toast.makeText(this, "パスワードが間違っています", Toast.LENGTH_SHORT).show();
        }
    }

    public void inputSettingPassword(String inputPassword) {
        if (!inputPassword.isEmpty()) {
            SharedPreferences preference = getSharedPreferences("Preference Name", MODE_PRIVATE);
            preference.edit().putString("SettingPassword", inputPassword).apply();
        } else {
            DialogFragment newFragment = new InputPasswordCustomDialog();
            newFragment.show(getFragmentManager(), InputPasswordCustomDialog.SITUATION_INPUT_SETTING_PASSWORD);
        }
    }

    public void categoryListSet() {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("data/data/" + getPackageName() + "/Sample.db", null);
        Cursor cursor = db.query("category", new String[]{"category", "isUnpublished"}, null, null, null, null, null);

        mDataList = new ArrayList<String>();
        if (cursor.moveToFirst()) {
            do {
                if(cursor.getInt(cursor.getColumnIndex("isUnpublished")) == 0) mDataList.add(cursor.getString(cursor.getColumnIndex("category")));
            } while (cursor.moveToNext());
        }

        mDataList.add("非表示フォルダ");

        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDataList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == mDataList.size() - 1) {
                    DialogFragment newFragment = new InputPasswordCustomDialog();
                    newFragment.show(getFragmentManager(), InputPasswordCustomDialog.SITUATION_SELECT_UNPUBLISHED_CATEGORY);
                } else {
                    mCategory = mDataList.get(position);

                    SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("data/data/" + getPackageName() + "/Sample.db", null);
                    Cursor cursor = db.query("category", new String[]{"_id", "password", "isLocked", "isUnpublished"}, "category = ?", new String[]{mCategory}, null, null, null);

                    if (cursor.moveToFirst()) {
                        mCategoryId = cursor.getInt(cursor.getColumnIndex("_id"));
                        mPassword = cursor.getString(cursor.getColumnIndex("password"));
                        if (cursor.getInt(cursor.getColumnIndex("isLocked")) == 0) {
                            Intent intent = new Intent(getApplicationContext(), FragmentActivity.class);
                            intent.putExtra("category", mCategory);
                            intent.putExtra("categoryId", mCategoryId);
                            startActivity(intent);
                        } else {
                            DialogFragment newFragment = new InputPasswordCustomDialog();
                            newFragment.show(getFragmentManager(), InputPasswordCustomDialog.SITUATION_SELECT_LOCKED_CATEGORY);
                        }
                    }
                }
            }
        });
    }
}
