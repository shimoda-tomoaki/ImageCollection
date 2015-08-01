package com.example.shimoda_tomoaki.helloworld;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class CategoryListActivity extends AppCompatActivity {
    ListView mListView;
    CategoryListAdapter mAdapter;
    ArrayList<CategoryListItem> mItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        getSupportActionBar().setTitle("フォルダリスト");

        SQLiteDatabase db = DBTools.getDatabase(this);
        Cursor cursor = db.query("category", new String[]{"_id", "category", "password", "isLocked", "isUnpublished"}, null, null, null, null, "_id");

        mItemList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String category = cursor.getString(cursor.getColumnIndex("category"));
                String password = cursor.getString(cursor.getColumnIndex("password"));
                String state =
                        cursor.getInt(cursor.getColumnIndex("isLocked")) == 1 ? CategoryListItem.STATE_LOCK :
                        cursor.getInt(cursor.getColumnIndex("isUnpublished")) == 1 ? CategoryListItem.STATE_UNPUBLISH : CategoryListItem.STATE_NORMAL;
                mItemList.add(new CategoryListItem(id, category, password, state));
            } while(cursor.moveToNext());
        }

        if (mItemList.size() == 0) {
            TextView noFolderMessageView = (TextView) findViewById(R.id.noFolderMessageView);
            noFolderMessageView.setVisibility(View.VISIBLE);
        } else {
            mAdapter = new CategoryListAdapter(this, R.layout.category_list_layout, mItemList);

            mListView = (ListView) findViewById(R.id.listView2);
            mListView.setAdapter(mAdapter);
            mListView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public class CategoryListItem {
        public static final String STATE_NORMAL = "フォルダ名表示";
        public static final String STATE_LOCK = "フォルダ名表示(パスワードあり)";
        public static final String STATE_UNPUBLISH = "フォルダ名非表示";

        private int mCategoryId;
        private String mCategory;
        private String mPassword;
        private String mState;

        public CategoryListItem(int categoryId, String category, String password, String state) {
            mCategoryId = categoryId;
            mCategory = category;
            mPassword = password;
            mState = state;
        }

        public int getCategoryId() { return mCategoryId; }
        public String getCategory() { return mCategory; }
        public String getPassword() { return mPassword; }
        public String getState() { return mState; }
    }

    public class CategoryListAdapter extends ArrayAdapter<CategoryListItem> {
        private LayoutInflater mLayoutInflater;

        public CategoryListAdapter(Context context, int textViewResourceId, List<CategoryListItem> objects) {
            super(context, textViewResourceId, objects);
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final CategoryListItem item = getItem(position);

            if (null == convertView) {
                convertView = mLayoutInflater.inflate(R.layout.category_list_layout, null);
            }

            ((TextView) convertView.findViewById(R.id.categoryTextView)).setText(item.getCategory());
            ((TextView) convertView.findViewById(R.id.passwordTextView)).setText("パスワード：" + item.getPassword());
            ((TextView) convertView.findViewById(R.id.stateTextView)).setText("設定：" + item.getState());

            (convertView.findViewById(R.id.button4)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("本当に削除しますか?")
                            .setPositiveButton("削除", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DBTools.removeCategory(getApplicationContext(), item.getCategoryId());
                                    mAdapter.remove(mItemList.get(position));

                                    if (mItemList.size() == 0) {
                                        TextView noFolderMessageView = (TextView) findViewById(R.id.noFolderMessageView);
                                        noFolderMessageView.setVisibility(View.VISIBLE);
                                    }
                                }
                            })
                            .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            })
                            .show();
                }
            });

            return convertView;
        }
    }
}
