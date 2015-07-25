package com.example.shimoda_tomoaki.helloworld;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by shimoda-tomoaki on 2015/07/19.
 */
public class ChangePasswordDialog extends DialogFragment {
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

                SharedPreferences preference = getActivity().getSharedPreferences("Preference Name", getActivity().MODE_PRIVATE);
                if (!oldPassword.equals(preference.getString("SettingPassword", ""))) {
                    Toast.makeText(getActivity(), "変更前のパスワードが間違っています", Toast.LENGTH_SHORT).show();
                } else if (newPassword.isEmpty()) {
                    Toast.makeText(getActivity(), "変更後のパスワードを入力してください", Toast.LENGTH_SHORT).show();
                } else {
                    preference.edit().putString("SettingPassword", newPassword).apply();
                    dismiss();
                    return;
                }

                DialogFragment newFragment = new ChangePasswordDialog();

                Bundle args = new Bundle();
                args.putString("oldPassword", oldPassword);
                args.putString("newPassword", newPassword);
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
