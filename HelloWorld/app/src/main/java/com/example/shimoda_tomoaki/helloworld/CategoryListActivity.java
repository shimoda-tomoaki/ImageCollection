package com.example.shimoda_tomoaki.helloworld;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class CategoryListActivity extends ActionBarActivity {
    ListView mListView;
    CategoryListAdapter mAdapter;
    ArrayList<CategoryListItem> mItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        SQLiteDatabase db = DBTools.getDatabase(this);
        Cursor cursor = db.query("category", new String[]{"_id", "category", "password", "isLocked", "isUnpublished"}, null, null, null, null, "_id");

        mItemList = new ArrayList<CategoryListItem>();
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

        mAdapter = new CategoryListAdapter(this, R.layout.category_list_layout, mItemList);

        mListView = (ListView) findViewById(R.id.listView2);
        mListView.setAdapter(mAdapter);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_category_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            final CategoryListItem item = (CategoryListItem)getItem(position);

            if (null == convertView) {
                convertView = mLayoutInflater.inflate(R.layout.category_list_layout, null);
            }

            ((TextView) convertView.findViewById(R.id.categoryTextView)).setText(item.getCategory());
            ((TextView) convertView.findViewById(R.id.passwordTextView)).setText("パスワード：" + item.getPassword());
            ((TextView) convertView.findViewById(R.id.stateTextView)).setText("設定：" + item.getState());


            ((Button) convertView.findViewById(R.id.button4)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DBTools.removeCategory(getApplicationContext(), item.getCategoryId());
                    mAdapter.remove(mItemList.get(position));
                }
            });

            return convertView;
        }
    }
}
