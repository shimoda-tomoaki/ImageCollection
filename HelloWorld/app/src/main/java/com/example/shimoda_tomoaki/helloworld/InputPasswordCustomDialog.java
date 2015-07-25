package com.example.shimoda_tomoaki.helloworld;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by shimoda-tomoaki on 2015/07/18.
 */
public class InputPasswordCustomDialog extends DialogFragment {
    public static final String SITUATION_SELECT_LOCKED_CATEGORY = "select_locked_category";
    public static final String SITUATION_SELECT_UNPUBLISHED_CATEGORY = "select_unpublished_category";
    public static final String SITUATION_SELECT_CATEGORY_LIST = "select_category_list";
    public static final String SITUATION_INPUT_SETTING_PASSWORD = "input_setting_password";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View content = inflater.inflate(R.layout.input_password_edit_text, null);

        builder.setView(content);

        String title = "";
        switch(getTag()) {
            case SITUATION_SELECT_LOCKED_CATEGORY:
            case SITUATION_SELECT_UNPUBLISHED_CATEGORY:
                title = "パスワードを入力してください";
                break;
            case SITUATION_SELECT_CATEGORY_LIST:
                title = "管理パスワードを入力してください";
                break;
            case SITUATION_INPUT_SETTING_PASSWORD:
                title = "管理パスワードを設定してください";
                break;
        }

        builder.setMessage(title).setPositiveButton("決定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String password = ((EditText) content.findViewById(R.id.input_password_edit_text)).getText().toString();

                switch(getTag()) {
                    case SITUATION_SELECT_LOCKED_CATEGORY:
                        ((TopActivity) getActivity()).selectLockedActivity(password);
                        break;
                    case SITUATION_SELECT_UNPUBLISHED_CATEGORY:
                        ((TopActivity) getActivity()).selectUnpublishedActivity(password);
                        break;
                    case SITUATION_SELECT_CATEGORY_LIST:
                        ((TopActivity) getActivity()).selectCategoryList(password);
                        break;
                    case SITUATION_INPUT_SETTING_PASSWORD:
                        ((TopActivity) getActivity()).inputSettingPassword(password);
                        break;
                }

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
