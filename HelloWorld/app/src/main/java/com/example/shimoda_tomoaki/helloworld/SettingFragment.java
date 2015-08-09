package com.example.shimoda_tomoaki.helloworld;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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
    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY = "category";

    private int mCategoryId;
    private String mCategory;
    private EditText mCategoryText;
    private EditText mPasswordText;
    private Boolean mIsLocked = false;
    private Boolean mIsUnpublished = false;

    public OnFragmentInteractionListener mListener;

    public static SettingFragment newInstance(int categoryId, String category) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCategoryId = getArguments().getInt(ARG_CATEGORY_ID);
            mCategory = getArguments().getString(ARG_CATEGORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);

        mCategoryText = (EditText) rootView.findViewById(R.id.editText2);
        mCategoryText.setText(mCategory);
        mPasswordText = (EditText) rootView.findViewById(R.id.editText3);

        SQLiteDatabase db = DBTools.getDatabase(getActivity());
        Cursor cursor = db.query("category", new String[]{"password", "isLocked", "isUnpublished"}, "id = ?", new String[]{"" + mCategoryId}, null, null, null);

        if (cursor.moveToFirst()) {
            mPasswordText.setText(cursor.getString(cursor.getColumnIndex("password")));
            mIsLocked = cursor.getInt(cursor.getColumnIndex("isLocked")) == 1;
            mIsUnpublished = cursor.getInt(cursor.getColumnIndex("isUnpublished")) == 1;
        } else {
            Toast.makeText(getActivity(), "categoryID : " + mCategoryId, Toast.LENGTH_LONG).show();
        }

        Button decideButton = (Button) rootView.findViewById(R.id.button2);
        decideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category = mCategoryText.getText().toString();
                String password = mPasswordText.getText().toString();

                DBTools dbTools = new DBTools(getActivity());
                if (category.isEmpty()) Toast.makeText(getActivity(), "フォルダ名を入力してください", Toast.LENGTH_SHORT).show();
                else if (dbTools.isExistCategory(mCategoryId, category)) Toast.makeText(getActivity(), "フォルダ：「" + category + "」はもうあります", Toast.LENGTH_SHORT).show();
                else if ((mIsLocked || mIsUnpublished) && password.isEmpty()) Toast.makeText(getActivity(), "パスワードを入力してください", Toast.LENGTH_SHORT).show();
                else if (mIsUnpublished && dbTools.isExistPasswordInUnPublic(mCategoryId, password)) Toast.makeText(getActivity(), "別のパスワードを入力してください", Toast.LENGTH_SHORT).show();
                else {
                    ContentValues values = new ContentValues();
                    values.put("category", category);
                    values.put("password", (mIsLocked || mIsUnpublished) ? password : "");
                    values.put("isLocked", mIsLocked);
                    values.put("isUnpublished", mIsUnpublished);
                    dbTools.mDb.update("category", values, "id = ?", new String[]{"" + mCategoryId});
                    getActivity().finish();
                }
            }
        });

        Button removeButton = (Button) rootView.findViewById(R.id.button3);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                dialogBuilder
                        .setTitle("本当に削除しますか?")
                        .setPositiveButton("削除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DBTools.removeCategory(getActivity(), mCategoryId);
                                startActivity(new Intent(getActivity(), TopActivity.class));
                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        })
                        .show();
            }
        });

        RadioGroup radioGroup = (RadioGroup) rootView.findViewById(R.id.RadioGroupCarrier);
        radioGroup.check(mIsLocked ? R.id.radioButton2 : mIsUnpublished ? R.id.radioButton3 : R.id.radioButton);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mIsLocked = checkedId == R.id.radioButton2;
                mIsUnpublished = checkedId == R.id.radioButton3;
                mPasswordText.setEnabled(mIsLocked || mIsUnpublished);
            }
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
