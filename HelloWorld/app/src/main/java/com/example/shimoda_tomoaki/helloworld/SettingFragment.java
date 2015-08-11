package com.example.shimoda_tomoaki.helloworld;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class SettingFragment extends Fragment {
    private static final String ARG_FOLDER_ID = "folderId";
    private static final String ARG_FOLDER_NAME = "folderName";

    private int mFolderId;
    private String mFolderName;
    private EditText mFolderTextView;
    private EditText mPasswordText;
    private Boolean mIsLocked = false;
    private Boolean mIsUnpublished = false;

    public OnFragmentInteractionListener mListener;

    public static SettingFragment newInstance(int categoryId, String category) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FOLDER_ID, categoryId);
        args.putString(ARG_FOLDER_NAME, category);
        fragment.setArguments(args);
        return fragment;
    }

    public SettingFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFolderId = getArguments().getInt(ARG_FOLDER_ID);
            mFolderName = getArguments().getString(ARG_FOLDER_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);

        mFolderTextView = (EditText) rootView.findViewById(R.id.editText2);
        mFolderTextView.setText(mFolderName);
        mPasswordText = (EditText) rootView.findViewById(R.id.editText3);

        SQLiteDatabase db = new DBManager(getActivity()).getDB();
        Cursor cursor = db.query(DBManager.TABLE_FOLDER,
                DBManager.getFolderColumnNames(),
                DBManager.COLUMN_ID + " = ?",
                new String[]{"" + mFolderId}, null, null, null);

        if (cursor.moveToFirst()) {
            mPasswordText.setText(cursor.getString(cursor.getColumnIndex(DBManager.COLUMN_PASSWORD)));
            mIsLocked = cursor.getInt(cursor.getColumnIndex(DBManager.COLUMN_IS_LOCKED)) == 1;
            mIsUnpublished = cursor.getInt(cursor.getColumnIndex(DBManager.COLUMN_IS_UNPUBLISHED)) == 1;
        }

        Button decideButton = (Button) rootView.findViewById(R.id.button2);
        decideButton.setOnClickListener(v -> {
            String category = mFolderTextView.getText().toString();
            String password = mPasswordText.getText().toString();

            DBManager dbManager = new DBManager(getActivity());
            if (category.isEmpty()) Toast.makeText(getActivity(), "フォルダ名を入力してください", Toast.LENGTH_SHORT).show();
            else if (dbManager.isExistCategory(mFolderId, category)) Toast.makeText(getActivity(), "フォルダ：「" + category + "」はもうあります", Toast.LENGTH_SHORT).show();
            else if ((mIsLocked || mIsUnpublished) && password.isEmpty()) Toast.makeText(getActivity(), "パスワードを入力してください", Toast.LENGTH_SHORT).show();
            else if (mIsUnpublished && dbManager.isExistPasswordInUnPublic(mFolderId, password)) Toast.makeText(getActivity(), "別のパスワードを入力してください", Toast.LENGTH_SHORT).show();
            else {
                ContentValues values = new ContentValues();
                values.put("category", category);
                values.put("password", (mIsLocked || mIsUnpublished) ? password : "");
                values.put("isLocked", mIsLocked);
                values.put("isUnpublished", mIsUnpublished);
                dbManager.getDB().update("category", values, "id = ?", new String[]{"" + mFolderId});
                getActivity().finish();
            }
        });

        Button removeButton = (Button) rootView.findViewById(R.id.button3);
        removeButton.setOnClickListener(v -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            dialogBuilder
                    .setTitle("本当に削除しますか?")
                    .setPositiveButton("削除", (dialog, which) -> {
                        DBManager.removeCategory(getActivity(), mFolderId);
                        startActivity(new Intent(getActivity(), TopActivity.class));
                    })
                    .setNegativeButton("キャンセル", (dialog, which) -> {})
                    .show();
        });

        RadioGroup radioGroup = (RadioGroup) rootView.findViewById(R.id.RadioGroupCarrier);
        radioGroup.check(mIsLocked ? R.id.radioButton2 : mIsUnpublished ? R.id.radioButton3 : R.id.radioButton);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            mIsLocked = checkedId == R.id.radioButton2;
            mIsUnpublished = checkedId == R.id.radioButton3;
            mPasswordText.setEnabled(mIsLocked || mIsUnpublished);
        });

        setPasswordTextEnabled();

        return rootView;
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

    private void setPasswordTextEnabled() {
        mPasswordText.setEnabled(mIsLocked || mIsUnpublished);
    }


}
