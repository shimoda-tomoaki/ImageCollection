package com.example.shimoda_tomoaki.helloworld;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;


public class InputCategory extends ActionBarActivity {
    private EditText mPasswordText;
    private Boolean mIsLocked = false;
    private Boolean mIsUnpublished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_category);

        mPasswordText = (EditText) findViewById(R.id.editText3);
        mPasswordText.setEnabled(false);

        Button okButton = (Button) findViewById(R.id.button2);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category = ((EditText) findViewById(R.id.editText2)).getText().toString();
                String password = mPasswordText.getText().toString();

                DBTools dbTools = new DBTools(getApplicationContext());
                if (category.isEmpty()) Toast.makeText(getApplicationContext(), "カテゴリーを入力してください", Toast.LENGTH_SHORT).show();
                else if (dbTools.isExistCategory(-1, category)) Toast.makeText(getApplicationContext(), "カテゴリー：「" + category + "」はもうあります", Toast.LENGTH_SHORT).show();
                else if ((mIsLocked || mIsUnpublished) && password.isEmpty()) Toast.makeText(getApplicationContext(), "パスワードを入力してください", Toast.LENGTH_SHORT).show();
                else if (mIsUnpublished && dbTools.isExistPasswordInUnPublic(-1, password)) Toast.makeText(getApplicationContext(), "別のパスワードを入力してください", Toast.LENGTH_SHORT).show();
                else {
                    ContentValues values = new ContentValues();
                    values.put("category", category);
                    values.put("password", (mIsLocked || mIsUnpublished) ? password : "");
                    values.put("isLocked", mIsLocked);
                    values.put("isUnpublished", mIsUnpublished);
                    dbTools.mDb.insert("category", null, values);
                    finish();
                }
            }
        });

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.RadioGroupCarrier);
        radioGroup.check(R.id.radioButton);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mIsLocked = checkedId == R.id.radioButton2;
                mIsUnpublished = checkedId == R.id.radioButton3;
                mPasswordText.setEnabled(mIsLocked || mIsUnpublished);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_input_category, menu);
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

    private void setPasswordTextEnabled() {
        mPasswordText.setEnabled(mIsLocked || mIsUnpublished);
    }
}
