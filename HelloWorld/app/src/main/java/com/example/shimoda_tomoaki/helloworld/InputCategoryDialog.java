package com.example.shimoda_tomoaki.helloworld;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;


public class InputCategoryDialog extends DialogFragment {
    private String mCategory = "";
    private String mPassword = "";
    private EditText mPasswordText;
    private boolean mIsLocked = false;
    private boolean mIsUnpublished = false;

    @Override
    public void setArguments(Bundle bundle) {
        mCategory = bundle.getString("category");
        mPassword = bundle.getString("password");
        mIsLocked = bundle.getBoolean("isLocked");
        mIsUnpublished = bundle.getBoolean("isUnpublished");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View content = inflater.inflate(R.layout.input_category_dialog, null);

        builder.setView(content);

        ((EditText) content.findViewById(R.id.editText2)).setText(mCategory);

        mPasswordText = (EditText) content.findViewById(R.id.editText3);
        mPasswordText.setText(mPassword);
        mPasswordText.setEnabled(mIsLocked || mIsUnpublished);

        RadioGroup radioGroup = (RadioGroup) content.findViewById(R.id.RadioGroupCarrier);
        radioGroup.check(mIsLocked ? R.id.radioButton2 : mIsUnpublished ? R.id.radioButton3 : R.id.radioButton);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mIsLocked = checkedId == R.id.radioButton2;
                mIsUnpublished = checkedId == R.id.radioButton3;
                mPasswordText.setEnabled(mIsLocked || mIsUnpublished);
            }
        });

        builder.setMessage("新しいフォルダを設定してください").setPositiveButton("決定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String category = ((EditText) content.findViewById(R.id.editText2)).getText().toString();
                String password = mPasswordText.getText().toString();

                DBTools dbTools = new DBTools(getActivity());

                if (category.isEmpty())
                    Toast.makeText(getActivity(), "カテゴリーを入力してください", Toast.LENGTH_SHORT).show();
                else if (dbTools.isExistCategory(-1, category))
                    Toast.makeText(getActivity(), "カテゴリー：「" + category + "」はもうあります", Toast.LENGTH_SHORT).show();
                else if ((mIsLocked || mIsUnpublished) && password.isEmpty())
                    Toast.makeText(getActivity(), "パスワードを入力してください", Toast.LENGTH_SHORT).show();
                else if (mIsUnpublished && dbTools.isExistPasswordInUnPublic(-1, password))
                    Toast.makeText(getActivity(), "別のパスワードを入力してください", Toast.LENGTH_SHORT).show();
                else {
                    ContentValues values = new ContentValues();
                    values.put("category", category);
                    values.put("password", (mIsLocked || mIsUnpublished) ? password : "");
                    values.put("isLocked", mIsLocked);
                    values.put("isUnpublished", mIsUnpublished);
                    dbTools.mDb.insert("category", null, values);

                    ((TopActivity) getActivity()).categoryListSet();

                    dismiss();
                    return;
                }
                DialogFragment newFragment = new InputCategoryDialog();
                Bundle args = new Bundle();
                args.putString("category", category);
                args.putString("password", password);
                args.putBoolean("isLocked", mIsLocked);
                args.putBoolean("isUnpublished", mIsUnpublished);
                newFragment.setArguments(args);
                newFragment.show(getFragmentManager(), "");
                dismiss();
            }
        }).setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });

        return builder.create();
    }
}
