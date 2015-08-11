package com.example.shimoda_tomoaki.helloworld;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;


public class CreateFolderActivity extends Activity {
    private EditText mPasswordText;
    private Boolean mIsLocked = false;
    private Boolean mIsUnpublished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_folder);

        mPasswordText = (EditText) findViewById(R.id.editText3);
        mPasswordText.setEnabled(false);

        Button okButton = (Button) findViewById(R.id.button2);
        okButton.setOnClickListener(v -> {
            String folderName = ((EditText) findViewById(R.id.editText2)).getText().toString();
            String password = mPasswordText.getText().toString();

            DBManager dbManager = new DBManager(getApplicationContext());
            if (folderName.isEmpty())
                Toast.makeText(getApplicationContext(), "カテゴリーを入力してください", Toast.LENGTH_SHORT).show();
            else if (dbManager.isExistCategory(-1, folderName))
                Toast.makeText(getApplicationContext(), "カテゴリー：「" + folderName + "」はもうあります", Toast.LENGTH_SHORT).show();
            else if ((mIsLocked || mIsUnpublished) && password.isEmpty())
                Toast.makeText(getApplicationContext(), "パスワードを入力してください", Toast.LENGTH_SHORT).show();
            else if (mIsUnpublished && dbManager.isExistPasswordInUnPublic(-1, password))
                Toast.makeText(getApplicationContext(), "別のパスワードを入力してください", Toast.LENGTH_SHORT).show();
            else {
                ContentValues values = new ContentValues();
                values.put(DBManager.COLUMN_FOLDER_NAME, folderName);
                values.put(DBManager.COLUMN_PASSWORD, (mIsLocked || mIsUnpublished) ? password : "");
                values.put(DBManager.COLUMN_IS_LOCKED, mIsLocked);
                values.put(DBManager.COLUMN_IS_UNPUBLISHED, mIsUnpublished);
                dbManager.getDB().insert(DBManager.TABLE_FOLDER, null, values);
                finish();
            }
        });




        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.RadioGroupCarrier);
        radioGroup.check(R.id.radioButton);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            mIsLocked = checkedId == R.id.radioButton2;
            mIsUnpublished = checkedId == R.id.radioButton3;
            mPasswordText.setEnabled(mIsLocked || mIsUnpublished);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_input_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
