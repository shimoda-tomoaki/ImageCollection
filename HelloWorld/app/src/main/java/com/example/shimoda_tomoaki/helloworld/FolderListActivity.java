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


public class FolderListActivity extends AppCompatActivity {
    ListView mListView;
    FolderListAdapter mAdapter;
    ArrayList<FolderListItem> mItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_list);

        getSupportActionBar().setTitle("フォルダリスト");

        SQLiteDatabase db = new DBManager(this).getDB();
        Cursor cursor = db.query(DBManager.TABLE_FOLDER, DBManager.getFolderColumnNames(), null, null, null, null, DBManager.COLUMN_ID);

        mItemList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(DBManager.COLUMN_ID));
                String folderName = cursor.getString(cursor.getColumnIndex(DBManager.COLUMN_FOLDER_NAME));
                String password = cursor.getString(cursor.getColumnIndex(DBManager.COLUMN_PASSWORD));
                String state =
                        cursor.getInt(cursor.getColumnIndex(DBManager.COLUMN_IS_LOCKED)) == 1 ? FolderListItem.STATE_LOCK :
                        cursor.getInt(cursor.getColumnIndex(DBManager.COLUMN_IS_UNPUBLISHED)) == 1 ? FolderListItem.STATE_UNPUBLISHED : FolderListItem.STATE_NORMAL;
                mItemList.add(new FolderListItem(id, folderName, password, state));
            } while(cursor.moveToNext());
        }

        if (mItemList.size() == 0) {
            TextView noFolderMessageView = (TextView) findViewById(R.id.noFolderMessageView);
            noFolderMessageView.setVisibility(View.VISIBLE);
        } else {
            mAdapter = new FolderListAdapter(this, R.layout.folder_list_layout, mItemList);

            mListView = (ListView) findViewById(R.id.listView2);
            mListView.setAdapter(mAdapter);
            mListView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public class FolderListItem {
        public static final String STATE_NORMAL = "フォルダ名表示";
        public static final String STATE_LOCK = "フォルダ名表示(パスワードあり)";
        public static final String STATE_UNPUBLISHED = "フォルダ名非表示";

        private int mCategoryId;
        private String mCategory;
        private String mPassword;
        private String mState;

        public FolderListItem(int categoryId, String category, String password, String state) {
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

    public class FolderListAdapter extends ArrayAdapter<FolderListItem> {
        private LayoutInflater mLayoutInflater;

        public FolderListAdapter(Context context, int textViewResourceId, List<FolderListItem> objects) {
            super(context, textViewResourceId, objects);
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final FolderListItem item = getItem(position);

            if (null == convertView) {
                convertView = mLayoutInflater.inflate(R.layout.folder_list_layout, null);
            }

            ((TextView) convertView.findViewById(R.id.categoryTextView)).setText(item.getCategory());
            ((TextView) convertView.findViewById(R.id.passwordTextView)).setText("パスワード：" + item.getPassword());
            ((TextView) convertView.findViewById(R.id.stateTextView)).setText("設定：" + item.getState());

            (convertView.findViewById(R.id.button4)).setOnClickListener(v -> new AlertDialog.Builder(getContext())
                    .setTitle("本当に削除しますか?")
                    .setPositiveButton("削除", (dialog, which) -> {
                        DBManager.removeCategory(getApplicationContext(), item.getCategoryId());
                        mAdapter.remove(mItemList.get(position));

                        if (mItemList.size() == 0) {
                            TextView noFolderMessageView = (TextView) findViewById(R.id.noFolderMessageView);
                            noFolderMessageView.setVisibility(View.VISIBLE);
                        }
                    })
                    .setNegativeButton("キャンセル", (dialog, which) -> {})
                    .show());

            return convertView;
        }
    }
}
