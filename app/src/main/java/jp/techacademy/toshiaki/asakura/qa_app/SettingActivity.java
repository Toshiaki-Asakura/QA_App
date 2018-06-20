package jp.techacademy.toshiaki.asakura.qa_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SettingActivity extends AppCompatActivity {  //--------------------------------------継承は無し

    DatabaseReference mDataBaseReference;  //-----------------------------------------------------データベースのメンバ変数
    private EditText mNameText;  //---------------------------------------------------------------EditText

//◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆onCreate◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);  //--------------------------------------------activity_setting画面を使用

//■■■表示名を取得してEditTextに反映させる
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);  //----Preference（端末の保存機構）から
        String name = sp.getString(Const.NameKEY, "");  //-------------------------------nameにPreferenceの主キー
        mNameText = (EditText) findViewById(R.id.nameText);  //-----------------------------------EditTextをidからmNameTextに設定
        mNameText.setText(name);  //---------------------------------------------------------------mNameTextをnameに⇒これでEditTextにPreferenceの主キーの名前を設定
//■■■
        mDataBaseReference = FirebaseDatabase.getInstance().getReference();  //-------------------データベース取得

//■■■UIの初期設定
        setTitle("設定");  //-----------------------------------------------------------------------タイトルを「設定」

//■■■ボタンの準備①changeButton「変更」ボタン■■■■■■■■■

        Button changeButton = (Button) findViewById(R.id.changeButton);  //------------------------changeButtonのインスタンス
        changeButton.setOnClickListener(new View.OnClickListener() {  //----------------------------リスナ実装

            @Override
            public void onClick(View v) {  //------------------------------------------------------押したら下記が起こる
                // キーボードが出ていたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();  //----------------ログイン済みのユーザーを取得する

                if (user == null) {  //-------------------------------------------------------------ユーザーがいなかったら表示
                    // ログインしていない場合は何もしない
                    Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG).show();
                    return;
                }  //-------------------------------------------------------------------------------ユーザーがいたら名前を変更できるらしい

                // 変更した表示名をFirebaseに保存する
                String name = mNameText.getText().toString();
                DatabaseReference userRef = mDataBaseReference.child(Const.UsersPATH).child(user.getUid());
                Map<String, String> data = new HashMap<String, String>();
                data.put("name", name);
                userRef.setValue(data);

                // 変更した表示名をPreferenceに保存する
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(Const.NameKEY, name);
                editor.commit();

                Snackbar.make(v, "表示名を変更しました", Snackbar.LENGTH_LONG).show();
            }
        });  //-------------------------------------------------------------------------------------リスナの中身は});

//■■■ボタンの準備①changeButton「ログアウト」ボタン■■■■■■■■■

        Button logoutButton = (Button) findViewById(R.id.logoutButton);  //------------------------ボタンの準備
        logoutButton.setOnClickListener(new View.OnClickListener() {  //----------------------------リスナ
            @Override
            public void onClick(View v) {  //------------------------------------------------------押したら下記が起こる
                FirebaseAuth.getInstance().signOut();  //-------------------------------------------サインアウト
                mNameText.setText("");  //---------------------------------------------------------EditTextを””
                Snackbar.make(v, "ログアウトしました", Snackbar.LENGTH_LONG).show();  //----スナックバーにログアウトの表示
            }
        });  //-------------------------------------------------------------------------------------リスナの中身は});
    }

//◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆onCreate finish◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆

}