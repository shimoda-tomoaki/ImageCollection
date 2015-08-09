package com.example.shimoda_tomoaki.helloworld;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TopActivity extends AppCompatActivity {
    private int mCategoryId;
    private String mCategory;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);

        SharedPreferences preference = getSharedPreferences("Preference Name", MODE_PRIVATE);
        if (!preference.getBoolean("Launched", false)) {
            preference.edit().putBoolean("Launched", true).apply();
        }

        if (!preference.getBoolean("DatabaseUpdated", false)) {
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("data/data/" + getPackageName() + "/Sample.db", null);
            db.execSQL("DROP TABLE IF EXISTS " + getString(R.string.table_category));
            db.execSQL("DROP TABLE IF EXISTS " + getString(R.string.table_url));
            db.execSQL("DROP TABLE IF EXISTS " + getString(R.string.table_image));
        }

        if (preference.getString("SettingPassword", "").isEmpty()) {
            showSettingMasterPasswordDialog(this);
        }

        getSupportActionBar().setTitle("画像収集アプリ");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings1) {
            DialogFragment newFragment = new InputCategoryDialog();
            newFragment.show(getFragmentManager(), null);
            return true;
        } else if (id == R.id.action_settings2) {
            showInputMasterPasswordDialog(this, "");
            return true;
        } else if (id == R.id.action_settings3) {
            new ChangeMasterPasswordDialog().show(getFragmentManager(), null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        categoryListSet();
    }

    public void categoryListSet() {
        ListView listView;
        FolderListItemAdapter folderListItemAdapter;
        ArrayList<FolderListItem> dataList;

        listView = (ListView) findViewById(R.id.listView);

        SQLiteDatabase db = new MySQLiteOpenHelper(this).getWritableDatabase();
        Cursor cursor = db.query("category", new String[]{"id", "category", "password", "isLocked", "isUnpublished"}, null, null, null, null, null);

        dataList = new ArrayList<>();
        while (cursor.moveToNext()) {
            if (cursor.getInt(cursor.getColumnIndex("isUnpublished")) == 0) {
                dataList.add(new FolderListItem(cursor.getInt(cursor.getColumnIndex("isLocked")) == 0 ? FolderItemType.NORMAL_FOLDER : FolderItemType.LOCK_FOLDER,
                        cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("category")),
                        cursor.getString(cursor.getColumnIndex("password"))));
            }
        }

        if (dataList.size() == 0) {
            dataList.add(new FolderListItem(FolderItemType.NO_FOLDER_MESSAGE));
        }

        dataList.add(new FolderListItem(FolderItemType.HIDE_FOLDER));

        folderListItemAdapter = new FolderListItemAdapter(this, R.layout.folder_item_list_layout, dataList);
        listView.setAdapter(folderListItemAdapter);
    }

    enum FolderItemType {NORMAL_FOLDER, LOCK_FOLDER, NO_FOLDER_MESSAGE, HIDE_FOLDER}

    public class FolderListItem {
        private static final String NO_FOLDER_MESSAGE = "表示するフォルダはありません\n右上のメニューからフォルダを作りましょう";
        private static final String HIDE_ITEM_MESSAGE = "非表示フォルダ";

        public FolderItemType mType;
        public int mCategoryId = -1;
        public String mItemName = "";
        public String mPassword = "";

        public FolderListItem(FolderItemType type) {
            mType = type;
            if (type == FolderItemType.NO_FOLDER_MESSAGE) {
                mItemName = NO_FOLDER_MESSAGE;
            } else if (type == FolderItemType.HIDE_FOLDER) {
                mItemName = HIDE_ITEM_MESSAGE;
            }
        }
        public FolderListItem(FolderItemType type, int categoryId, String itemName, String password) {
            mType = type;
            mCategoryId = categoryId;
            mItemName = itemName;
            mPassword = password;
        }

        public FolderItemType getType() { return mType; }
        public int getCategoryId() { return mCategoryId; }
        public String getItemName() { return mItemName; }
        public String getPassword() { return mPassword; }

    }

    public class FolderListItemAdapter extends ArrayAdapter<FolderListItem> {
        private LayoutInflater mLayoutInflater;

        public FolderListItemAdapter(Context context, int textViewResourceId, List<FolderListItem> objects) {
            super(context, textViewResourceId, objects);
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FolderListItem item = getItem(position);

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.folder_item_list_layout, null);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.titleTextView);
            textView.setText(item.getItemName());

            ImageView lockIconImageView = (ImageView) convertView.findViewById(R.id.lockMarkImageView);

            final int categoryId = item.getCategoryId();
            final String category = item.getItemName();
            final String password = item.getPassword();

            switch (item.getType()) {
                case NORMAL_FOLDER:
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(), FragmentActivity.class);
                            intent.putExtra("category", category);
                            intent.putExtra("categoryId", categoryId);
                            startActivity(intent);
                        }
                    });
                    textView.setTextColor(Color.BLACK);
                    textView.setTextSize(20.0f);
                    lockIconImageView.setVisibility(View.GONE);
                    break;
                case LOCK_FOLDER:
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mCategoryId = categoryId;
                            mCategory = category;
                            mPassword = password;
                            showInputLockFolderPasswordDialog(getContext(), "");
                        }
                    });
                    textView.setTextColor(Color.BLACK);
                    textView.setTextSize(20.0f);
                    lockIconImageView.setVisibility(View.VISIBLE);
                    lockIconImageView.setImageResource(R.drawable.ic_action_lock_closed);
                    break;
                case HIDE_FOLDER:
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showInputPrivateFolderPasswordDialog(getContext(), "");
                        }
                    });
                    textView.setTextColor(Color.GRAY);
                    textView.setTextSize(18.0f);
                    lockIconImageView.setVisibility(View.GONE);
                    break;
                case NO_FOLDER_MESSAGE:
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {}
                    });

                    textView.setTextColor(Color.GRAY);
                    textView.setTextSize(15.0f);
                    lockIconImageView.setVisibility(View.GONE);
                    break;
                default:
            }

            return convertView;
        }
    }

    public void showInputLockFolderPasswordDialog(final Context context, String inputPassword) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View content = inflater.inflate(R.layout.input_password_edit_text, null);
        final EditText passwordEditText = (EditText) content.findViewById(R.id.input_password_edit_text);
        passwordEditText.setText(inputPassword);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setView(content)
                .setMessage("パスワードを入力してください")
                .setPositiveButton("決定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String inputPassword = passwordEditText.getText().toString();

                        if (inputPassword.equals(mPassword)) {
                            Intent intent = new Intent(getApplicationContext(), FragmentActivity.class);
                            intent.putExtra("category", mCategory);
                            intent.putExtra("categoryId", mCategoryId);
                            startActivity(intent);
                        } else {
                            showInputLockFolderPasswordDialog(context, inputPassword);
                            Toast.makeText(context, "パスワードが間違っています", Toast.LENGTH_SHORT).show();
                        }

                        dialog.dismiss();
                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public void showInputPrivateFolderPasswordDialog(final Context context, String inputPassword) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View content = inflater.inflate(R.layout.input_password_edit_text, null);
        final EditText passwordEditText = (EditText) content.findViewById(R.id.input_password_edit_text);
        passwordEditText.setText(inputPassword);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setView(content)
                .setMessage("パスワードを入力してください")
                .setPositiveButton("決定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String inputPassword = passwordEditText.getText().toString();

                        SQLiteDatabase db = DBTools.getDatabase(context);
                        Cursor cursor = db.query("category", new String[]{"id", "category"}, "password = ? AND isUnpublished = ?", new String[]{inputPassword, "1"}, null, null, null);

                        if (cursor.moveToFirst()) {
                            Intent intent = new Intent(getApplicationContext(), FragmentActivity.class);
                            intent.putExtra("category", cursor.getString(cursor.getColumnIndex("category")));
                            intent.putExtra("categoryId", cursor.getInt(cursor.getColumnIndex("id")));
                            startActivity(intent);
                        } else {
                            showInputPrivateFolderPasswordDialog(context, inputPassword);
                            Toast.makeText(context, "入力したパスワードの非表示フォルダは存在しません", Toast.LENGTH_SHORT).show();
                        }

                        dialog.dismiss();
                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void showSettingMasterPasswordDialog(final Context context) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View content = inflater.inflate(R.layout.setting_master_password, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setView(content);

        dialog.setPositiveButton("決定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String password = ((EditText) content.findViewById(R.id.input_password_edit_text)).getText().toString();

                if (!password.isEmpty()) {
                    SharedPreferences preference = context.getSharedPreferences("Preference Name", MODE_PRIVATE);
                    preference.edit().putString("SettingPassword", password).apply();
                } else {
                    Toast.makeText(context, "管理パスワードを設定してください", Toast.LENGTH_LONG).show();
                    showSettingMasterPasswordDialog(context);
                }

                dialog.dismiss();
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(context, "管理パスワードを設定してください", Toast.LENGTH_SHORT).show();
                showSettingMasterPasswordDialog(context);
            }
        }).show();
    }

    public void showInputMasterPasswordDialog(final Context context, String inputPassword) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View content = inflater.inflate(R.layout.input_password_edit_text, null);
        final EditText passwordEditText = (EditText) content.findViewById(R.id.input_password_edit_text);
        passwordEditText.setText(inputPassword);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setView(content);

        dialog.setMessage("管理パスワードを入力してください").setPositiveButton("決定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String inputPassword = passwordEditText.getText().toString();

                SharedPreferences preference = context.getSharedPreferences("Preference Name", MODE_PRIVATE);

                if(inputPassword.equals(preference.getString("SettingPassword", ""))) {
                    context.startActivity(new Intent(context, CategoryListActivity.class));
                } else {
                    showInputMasterPasswordDialog(context, inputPassword);
                    Toast.makeText(context, "パスワードが間違っています", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            }
        }).setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        }).show();
    }

    public static class ChangeMasterPasswordDialog extends DialogFragment {
        private String mOldPassword = "";
        private String mNewPassword = "";

        @Override
        public void setArguments(Bundle bundle) {
            mOldPassword = bundle.getString("oldPassword", "");
            mNewPassword = bundle.getString("newPassword", "");
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View content = inflater.inflate(R.layout.change_password_dialog, null);

            builder.setView(content);
            ((EditText) content.findViewById(R.id.input_old_password_edit_text)).setText(mOldPassword);
            ((EditText) content.findViewById(R.id.input_new_password_edit_text)).setText(mNewPassword);

            builder.setTitle("管理パスワードを変更します").setPositiveButton("決定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    String oldPassword = ((EditText) content.findViewById(R.id.input_old_password_edit_text)).getText().toString();
                    String newPassword = ((EditText) content.findViewById(R.id.input_new_password_edit_text)).getText().toString();

                    SharedPreferences preference = getActivity().getSharedPreferences("Preference Name", MODE_PRIVATE);
                    if (!oldPassword.equals(preference.getString("SettingPassword", ""))) {
                        Toast.makeText(getActivity(), "変更前のパスワードが間違っています", Toast.LENGTH_SHORT).show();
                    } else if (newPassword.isEmpty()) {
                        Toast.makeText(getActivity(), "変更後のパスワードを入力してください", Toast.LENGTH_SHORT).show();
                    } else {
                        preference.edit().putString("SettingPassword", newPassword).apply();
                        dismiss();
                        return;
                    }

                    Bundle args = new Bundle();
                    args.putString("oldPassword", oldPassword);
                    args.putString("newPassword", newPassword);

                    DialogFragment newFragment = new ChangeMasterPasswordDialog();
                    newFragment.setArguments(args);
                    newFragment.show(getFragmentManager(), null);
                }
            }).setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dismiss();
                }
            });

            return builder.create();
        }
    }
}
