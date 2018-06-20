package jp.techacademy.toshiaki.asakura.qa_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText mEmailEditText; //-------------------------------------------------------------------メールのメンバ変数
    EditText mPasswordEditText;  //---------------------------------------------------------------パスワードのメンバ変数
    EditText mNameEditText;  //--------------------------------------------------------------------前のメンバ変数
    ProgressDialog mProgress;  //------------------------------------------------------------------進行状況を表示するプログレスダイアログのメンバ変数

    FirebaseAuth mAuth;  //-------------------------------------------------------------------------オーセンティケーション（認証・検証・資格有無）のメンバ変数
    OnCompleteListener<AuthResult> mCreateAccountListener;  //------------------------------------アカウント確認のメンバ変数
    OnCompleteListener<AuthResult> mLoginListener;  //---------------------------------------------ログイン確認のメンバ変数
    DatabaseReference mDataBaseReference;  //-----------------------------------------------------参照・取得したデータベース。のメンバ変数
    // データベースからデータを読み取るには、次に示す DatabaseReference のインスタンスが必要です。

    // アカウント作成時にフラグを立て、ログイン処理後に名前をFirebaseに保存する
    boolean mIsCreateAccount = false;  //--------------------------------------------------------フラグははじめは「false」で行こう！

//◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆onCreate◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);  //-----------------------------------------------レイアウトはログインを使用

        Log.d("asat","LoginActivity onCreate開始");//━■━■━■━■━
    //■■■データベースへのリファレンスを取得

        mDataBaseReference = FirebaseDatabase.getInstance().getReference();  //-------------------データベースをインスタンス化
        mAuth = FirebaseAuth.getInstance();  //---------------------------------------------------⁻FirebaseAuthのオブジェクトを取得する

    //■■■アカウント作成処理のリスナーをインスタンス化・実装

        mCreateAccountListener = new OnCompleteListener<AuthResult>() {  //----------------------OnCompleteListenerがアカウント作成処理の受け取り
            @Override
            public void onComplete(Task<AuthResult> task) {  //------------------------------------Task Task(View v)みたいなものか？onCompleteで正解かを取得

                if (task.isSuccessful()) {  //------------------------------------------------------成功したらログイン

                    String email = mEmailEditText.getText().toString();  //------------------------Eメール用Edittextの値を「email」に
                    String password = mPasswordEditText.getText().toString();  //-----------------パスワード用のEdittextの値を「password」に
                    login(email, password);  //-----------------------------------------------------⇒■■■個別メソッド名「login」■■■

                } else {  //------------------------------------------------------------------------駄目だったら

                    View view = findViewById(android.R.id.content);  //-----------------------------内部的に自動でContentViewと呼ばれるビューに割り当て
                    Snackbar.make(view, "アカウント作成に失敗しました", Snackbar.LENGTH_LONG).show();  //そいつをスナックバーに表示

                    mProgress.dismiss();  //-------------------------------------------------------プログレスダイアログを非表示にする、勝手に表示されるんか？
                }
            }
        };

    //■■■ログイン処理のリスナーのインスタンス化・実装

        mLoginListener = new OnCompleteListener<AuthResult>() {  //-------------------------------ログイン状況もOnCompleteListenerで受け取り
            @Override
            public void onComplete(Task<AuthResult> task) {  //------------------------------------Task Task(View v)みたいなものか？要件があってるかどうかを尋ねてそう

                if (task.isSuccessful()) {  //------------------------------------------------------LoginActhivity成功したらログイン

                    FirebaseUser user = mAuth.getCurrentUser();  //--------------------------------ログインしているユーザーを取得
                    DatabaseReference userRef = mDataBaseReference.child(Const.UsersPATH).child(user.getUid());
                                                                      //----------------------------ユーザーの中から絞り込んだものが「userRef」

                                if (mIsCreateAccount) {  //---------------------------------------もともと「false」設定なので
                                    Log.d("asat","新規アカウント");//━■━■━■━■━
                                    String name = mNameEditText.getText().toString();  //---------表示用Edittextの値をStringで「name」

                                    Map<String, String> data = new HashMap<String, String>();  //---こここでキーと値を宣言して初期化「data」という名のオブジェクトに
                                    data.put("name", name);  //-------------------------------------で「name」ってキーにアカウント時の表示名をセットして
                                    userRef.setValue(data);  //-------------------------------------Firebaseにデータを保存
                                    saveName(name);  //---------------------------------------------⇒■■■メソッド名「saveName」■■■

                                } else {
                                    Log.d("asat","ログイン");//━■━■━■━■━
                                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {
                                            Map data = (Map) snapshot.getValue();
                                            saveName((String)data.get("name"));
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError firebaseError) {
                                        }
                                    });
                                }

                    mProgress.dismiss();
                    finish();

                } else {  //-----------------------------------------------------------------------成功以外ならスナックバーに失敗を表示

                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show();

                    mProgress.dismiss();
                }
            }
        };

    //■■■タイトルバーのタイトルを変更

          setTitle("ログイン");

    //■■■UIの準備

        mEmailEditText = (EditText) findViewById(R.id.emailText);  //----------------------------メール用Edittextの実態を作る作る
        mPasswordEditText = (EditText) findViewById(R.id.passwordText);  //---------------------Eパスワード用dittextの実態を作る
        mNameEditText = (EditText) findViewById(R.id.nameText);  //------------------------------表示名用Edittextの実態を作る

        mProgress = new ProgressDialog(this);  //-----------------------------------------プログレスダイヤログのインスタンス
        mProgress.setMessage("処理中...");  //----------------------------------------------------ダイアログのメッセージは「処理中」
        Log.d("asat","処理中...");//━■━■━■━■━

    //■■■ボタンの準備①アカウント作成ボタン■■■■■■■■■

        Button createButton = (Button) findViewById(R.id.createButton);  //------------------------createButtonを実態か
        Log.d("asat","アカウントボタン準備");//━■━■━■━■━
        createButton.setOnClickListener(new View.OnClickListener() {  //----------------------------View.onClickListener実装

            @Override
            public void onClick(View v) {  //------------------------------------------------------押す

                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); //わからん、おまじないかな
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);// キーボードが出てたら閉じる

                String email = mEmailEditText.getText().toString();  //---------------------------Eメール用Edittextの値を「email」に
                String password = mPasswordEditText.getText().toString();  //---------------------パスワード用のEdittextの値を「password」に
                String name = mNameEditText.getText().toString();  //------------------------------表示名用のEditTextの値を「name」に

                if (email.length() != 0 && password.length() >= 6 && name.length() != 0) {  //------メール・表示名が""ではなく、PWが6文字以上なら
                    // ログイン時に表示名を保存するようにフラグを立てる
                    mIsCreateAccount = true;

                    createAccount(email, password);  //---------------------------------------------⇒■■■個別メソッド名「createAcount()」■■■
                } else {
                    // エラーを表示する
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();
                }
            }
        });

    //■■■ログインボタンのOnClickListenerを設定■■■■■■■■■

        Button loginButton = (Button) findViewById(R.id.loginButton);  //--------------------------loginButtonにリスナーを実装
        Log.d("asat","ログインボタン準備");//━■━■━■━■━
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  //------------------------------------------------------ クリックしたときにキーボードが出てたら閉じる、書き換えないように

                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String email = mEmailEditText.getText().toString();  //---------------------------Eメール用Edittextの値を「email」に
                String password = mPasswordEditText.getText().toString();  //---------------------パスワード用のEdittextの値を「password」に

                if (email.length() != 0 && password.length() >= 6) {
                    // フラグを落としておく
                    mIsCreateAccount = false;

                    login(email, password);  //-----------------------------------------------------⇒■■■個別メソッド名「login」■■■
                } else {
                    // エラーを表示する
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

//◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆onCreate finish◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆

    private void createAccount(String email, String password) {  //--------------------------------■■■個別メソッド名「createAcount()」■■■
        // プログレスダイアログを表示する
        mProgress.show();  //----------------------------------------------------------------------進行状況ダイアログを表示

        // アカウントを作成する
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener);
        Log.d("asat","アカウント作成");//━■━■━■━■━     //----------------------Firebaceにアカウントを作る（引数：メール、パスワード）
    }

    private void login(String email, String password) {//------------------------------------------■■■個別メソッド名「login」■■■
        // プログレスダイアログを表示する
        mProgress.show();  //----------------------------------------------------------------------進行状況ダイアログを表示

        // ログインする
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener);
                                                                    //------------------------------Firebaceにログインさせる（引数：メール、パスワード）
        Log.d("asat","ログイン処理");//━■━■━■━■━
    }

    private void saveName(String name) {//---------------------------------------------------------■■■個別メソッド名「saveName」■■■
        // Preferenceに保存する
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);  //----アプリの設定を端末に保存(Preferenceに保存する)
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Const.NameKEY, name);
        editor.commit();
        Log.d("asat","表示名保存");//━■━■━■━■━
    }
}