package com.example.shimoda_tomoaki.helloworld;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


public class InputFolderDialog extends DialogFragment {
    static final String ARG_FOLDER_NAME = "folderName";
    static final String ARG_PASSWORD = "password";
    static final String ARG_IS_LOCKED = "isLocked";
    static final String ARG_IS_UNPUBLISHED = "isUnpublished";

    private String mFolderName = "";
    private String mPassword = "";
    private EditText mPasswordText;
    private boolean mIsLocked = false;
    private boolean mIsUnpublished = false;

    @Override
    public void setArguments(Bundle bundle) {
        mFolderName = bundle.getString(ARG_FOLDER_NAME);
        mPassword = bundle.getString(ARG_PASSWORD);
        mIsLocked = bundle.getBoolean(ARG_IS_LOCKED);
        mIsUnpublished = bundle.getBoolean(ARG_IS_UNPUBLISHED);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View content = inflater.inflate(R.layout.create_folder_dialog, null, false);

        builder.setView(content);

        ((EditText) content.findViewById(R.id.editText2)).setText(mFolderName);

        final TextView passwordTextView = (TextView) content.findViewById(R.id.textView2);
        mPasswordText = (EditText) content.findViewById(R.id.editText3);
        mPasswordText.setText(mPassword);
        if (mIsLocked || mIsUnpublished){
            passwordTextView.setVisibility(View.VISIBLE);
            mPasswordText.setVisibility(View.VISIBLE);
        } else {
            passwordTextView.setVisibility(View.GONE);
            mPasswordText.setVisibility(View.GONE);
        }
        mPasswordText.setVisibility(mIsLocked || mIsUnpublished ? View.VISIBLE : View.GONE);

        RadioGroup radioGroup = (RadioGroup) content.findViewById(R.id.RadioGroupCarrier);
        radioGroup.check(mIsLocked ? R.id.radioButton2 : mIsUnpublished ? R.id.radioButton3 : R.id.radioButton);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            mIsLocked = checkedId == R.id.radioButton2;
            mIsUnpublished = checkedId == R.id.radioButton3;
            if (mIsLocked || mIsUnpublished){
                passwordTextView.setVisibility(View.VISIBLE);
                mPasswordText.setVisibility(View.VISIBLE);
            } else {
                passwordTextView.setVisibility(View.GONE);
                mPasswordText.setVisibility(View.GONE);
            }
        });

        builder.setMessage("新しいフォルダを設定してください").setPositiveButton("決定", (dialog, id) -> {
            String folderName = ((EditText) content.findViewById(R.id.editText2)).getText().toString();
            String password = mPasswordText.getText().toString();

            DBManager dbManager = new DBManager(getActivity());

            if (folderName.isEmpty()) Toast.makeText(getActivity(), "フォルダ名を入力してください", Toast.LENGTH_SHORT).show();
            else if (dbManager.isExistCategory(-1, folderName)) Toast.makeText(getActivity(), "フォルダ「" + folderName + "」はもうあります", Toast.LENGTH_SHORT).show();
            else if ((mIsLocked || mIsUnpublished) && password.isEmpty()) Toast.makeText(getActivity(), "パスワードを入力してください", Toast.LENGTH_SHORT).show();
            else if (mIsUnpublished && dbManager.isExistPasswordInUnPublic(-1, password)) Toast.makeText(getActivity(), "別のパスワードを入力してください", Toast.LENGTH_SHORT).show();
            else {
                ContentValues values = new ContentValues();
                values.put(DBManager.COLUMN_FOLDER_NAME, folderName);
                values.put(DBManager.COLUMN_PASSWORD, (mIsLocked || mIsUnpublished) ? password : "");
                values.put(DBManager.COLUMN_IS_LOCKED, mIsLocked);
                values.put(DBManager.COLUMN_IS_UNPUBLISHED, mIsUnpublished);
                dbManager.getDB().insert(DBManager.TABLE_FOLDER, null, values);

                ((TopActivity) getActivity()).folderListSet();

                dismiss();
                return;
            }
            DialogFragment newFragment = new InputFolderDialog();
            Bundle args = new Bundle();
            args.putString(ARG_FOLDER_NAME, folderName);
            args.putString(ARG_PASSWORD, password);
            args.putBoolean(ARG_IS_LOCKED, mIsLocked);
            args.putBoolean(ARG_IS_UNPUBLISHED, mIsUnpublished);
            newFragment.setArguments(args);
            newFragment.show(getFragmentManager(), "");
            dismiss();
        }).setNegativeButton("キャンセル", (dialog, id) -> {
            dismiss();
        });

        return builder.create();
    }
}
