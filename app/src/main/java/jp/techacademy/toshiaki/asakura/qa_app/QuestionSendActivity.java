package jp.techacademy.toshiaki.asakura.qa_app;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class QuestionSendActivity extends AppCompatActivity implements View.OnClickListener, DatabaseReference.CompletionListener {



    private static final int PERMISSIONS_REQUEST_CODE = 100;  //------------------------------でましたパーミッションコード
    private static final int CHOOSER_REQUEST_CODE = 100;  //-----------------------------------何のコードか後でわかるでしょう。

    private ProgressDialog mProgress;  //---------------------------------------------------------進行ダイアログ
    private EditText mTitleText;  //--------------------------------------------------------------EditText１のメンバ変数タイトル
    private EditText mBodyText;  //---------------------------------------------------------------同じく本文
    private ImageView mImageView;  //-------------------------------------------------------------イメージを変更するためのメンバ変数
    private Button mSendButton;  //---------------------------------------------------------------送信ボタンの変数

    private int mGenre;  //-----------------------------------------------------------------------カテゴリー識別用int
    private Uri mPictureUri;  //------------------------------------------------------------------写真のURI（住所）

//◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆onCreate◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆

    @Override
    protected void onCreate(Bundle savedInstanceState) {  //--------------------------------------activity_question_sendの設定・描画開始
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_send);

                                                                                                    //バンドルを使って保存されていたインテント前のデータを取り出し、mGenreのメンバ変数に登録それを使って
                                                                                                    //タイトルの名前を変えているだけ。

        Bundle extras = getIntent().getExtras();  //------------------------------------------------渡ってきたジャンルの番号を保持する「カテゴリ」の番号
        mGenre = extras.getInt("genre");  //-------------------------------------------------intent.putExtra("genre", mGenre); の裏返し

        // UIの準備
        if (mGenre == 1) {
            setTitle("「趣味」質問作成");
        } else if (mGenre == 2) {
            setTitle("「生活」質問作成");
        } else if (mGenre == 3) {
            setTitle("「健康」質問作成");
        } else if (mGenre == 4) {
            setTitle("「PC」質問作成");
        } else {
            return;
        }

        //EditTextはViewから中身を変数化、ボタン、イメージには変数化してリスナ登録
        mTitleText = (EditText) findViewById(R.id.titleText);  //---------------------------------UI部品セット
        mBodyText = (EditText) findViewById(R.id.bodyText);//-------------------------------------UI部品セット

        mSendButton = (Button) findViewById(R.id.sendButton);//-----------------------------------UI部品セット
        mSendButton.setOnClickListener(this);  //-------------------------------------------------リスナ実装

        mImageView = (ImageView) findViewById(R.id.imageView);//----------------------------------UI部品セット
        mImageView.setOnClickListener(this);  //--------------------------------------------------リスナ実装

        mProgress = new ProgressDialog(this);  //-----------------------------------------ダイアログ準備
        mProgress.setMessage("投稿中...");  //----------------------------------------------------ダイアログ表示「投稿中」

    }

//◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆onCreate finish◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆


//■■■ボタンの準備①

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSER_REQUEST_CODE) {  //-------------------------------------------CHOOSER_REQUEST_CODEが設定してあれば、ちなみにこの子は100

                                                                                                    Log.d("asat","■105■requestCode:" +String.valueOf(requestCode));
                                                                                                    Log.d("asat","■107■resultCode:" +String.valueOf(resultCode));
                                                                                                    Log.d("asat","■109■data:" +String.valueOf(data));

            if (resultCode != RESULT_OK) {  //-----------------------------------------------------各機能の使用コードが合わないときに
                if (mPictureUri != null) {  //----------------------------------------------------写真URIがnullじゃなければ
                    getContentResolver().delete(mPictureUri, null, null);//---写真のURIを削除
                    mPictureUri = null;  //--------------------------------------------------------あらためてmPictureUriもnullにしておく
                }
                return;
            }

            // 画像を取得
            Uri uri;  //----------------------------------------------------------------------------画像の素性で指定するuri の内容が変わる
            if (data == null) {  //----------------------------------------------------------------カメラで撮影したときは「mPictureUri」はnull
                uri = mPictureUri;
            } else if (data.getData() == null) {//-------------------------------------------------カメラで撮影したとき「mPictureUri」
                uri = mPictureUri;
            } else {
                uri = data.getData();  //-----------------------------------------------------------ギャラリーからなので「data.getData()」
            }
                                                                                                    Log.d("asat","■129■uri:" +String.valueOf(uri));//
                                                                                                    // ━■━■━■━■━

            Bitmap image;  //-----------------------------------------------------------------------URIからBitmapを取得する
            try {  //-------------------------------------------------------------------------------トライキャッチで画像をbitmapにして受け取るとか技術的なことかな
                ContentResolver contentResolver = getContentResolver();
                InputStream inputStream = contentResolver.openInputStream(uri);
                image = BitmapFactory.decodeStream(inputStream);
                inputStream.close();  //------------------------------------------------------------取得したら画面を閉じて
            } catch (Exception e) {
                return;
            }

            int imageWidth = image.getWidth();                                                      //--------取得したBimapの長辺を500ピクセルにリサイズするという技術的な記述
            int imageHeight = image.getHeight();
            float scale = Math.min((float) 500 / imageWidth, (float) 500 / imageHeight); // (1)

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);                                                         //----これも画像を土の大きさで映し出すかという技術的な記述

            Bitmap resizedImage =  Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true);//リサイズしての･･･最終名「resizedImage」

            // BitmapをImageViewに設定する
            mImageView.setImageBitmap(resizedImage);                                                  //-------------------------------------------画面のImageView「mImageView」に選択した画像をセット

            mPictureUri = null;                                                                      //---------------------------------------------------------------あらためてmPictureUriもnullにしておく
        }
    }


//■■■onClick■■■する（中身はViewのIDによっていろいろなボタンが押される処理に切り替わる）
    @Override
    public void onClick(View v) {                                                                    //---------------ボタンを押したときに

        //■■■もしもImageViewをクリックしたら
            if (v == mImageView) {                                                                  //----------ImageViewにセットしたリスナで「画像を押すってことね」

                 // パーミッションの許可状態を確認する
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {                               //------SDKのビルド番号が新しいとインストール時ではなくその場で聞かれる⇒ダイヤログ
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                                                                                   // 許可されている
                   showChooser();                                                                   //----------許可されてるから■■■showChooser■■■
                    } else {
                        // 許可されていないので許可ダイアログを表示する
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                        //--されていないので■■■onRequestPermissionsResult■■■
                        return;
                    }
                } else {
                        showChooser();  //--------------------------------------------------------------番号が古くパーミッションが関係ない時も■■■showChooser■■■
                }

        //■■■もしもセンドボタンををクリックしたら
            } else if (v == mSendButton) {
                                                                                                   //送信ボタンを押したらキーボードを閉じて全画面表示⇒「mProgress」
            InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            Log.d("asat","■186■mSendButton「キーボード消しましたよ」");//

                                                                                                   //■◆■◆■◆■◆■◆■◆■◆■◆■◆■◆■◆■◆■◆■◆■◆■◆■◆■◆■◆■◆■◆■データベースの取得とアドレスを取得
                                                                                                   //Firebaseにつないで、リファレンス全体のインスタンスを作成「dataBaseReference」
            DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();    //Firebaseにつなぐおまじない
                                                                                                               //その「dataBaseReference」の中の子階層「.child(Const.ContentsPATH)」のさらに子階層「.child(String.valueOf(mGenre)」を「genreRef」
            DatabaseReference genreRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));

                                                                                                    Log.d("asat","■191■mGenre："+String.valueOf(mGenre));
                                                                                                    Log.d("asat","■192■ContentsPATH："+String.valueOf(dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre))));
                                                                                                    Log.d("asat","■193■genreRef："+String.valueOf(genreRef));
            Map<String, String> data = new HashMap<String, String>();  //---------------------------------------mapデータの宣言・初期化（データを取り直すため）

            // UIDだれが入力したかプッシュ
            data.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());  //-------------map<"uid,ユーザーのUid">

                                                                                                    Log.d("asat","■199■FirebaseAuth.getInstance().getCurrentUser().getUid()："+String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getUid()));



            //センドクエスションの空白などがあればスナックバーにさし戻れ覚ます。リターンでやり直し
                                                                                                    //-------------------------------------------------------------------------------------------------------------------------------------------
            String title = mTitleText.getText().toString();  //------------------------------------title
            String body = mBodyText.getText().toString();  //--------------------------------------body

            if (title.length() == 0) {
                // 質問が入力されていない時はエラーを表示するだけ
                Snackbar.make(v, "タイトルを入力して下さい", Snackbar.LENGTH_LONG).show();
                return;   //                                                                               ※EditTextに空白があれば差し戻される！
            }

            if (body.length() == 0) {
                // 質問が入力されていない時はエラーを表示するだけ
                Snackbar.make(v, "質問を入力して下さい", Snackbar.LENGTH_LONG).show();
                return;
            }
                                                                                                    Log.d("asat","■216■mSendButton「空白ないですよ」");
                                                                                                    //-------------------------------------------------------------------------------------------------------------------------------------------

            // Preferenceから名前を取る
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String name = sp.getString(Const.NameKEY, "");  //----------------------------プリファレンスのnameを取得して"表示名"にセット

            data.put("title", title);
                                                                                                    Log.d("asat","■227■タイトルは："+String.valueOf(title));
            data.put("body", body);
                                                                                                    Log.d("asat","■229■本文は："+String.valueOf(body));
            data.put("name", name);
                                                                                                    Log.d("asat","■231■表示名は："+String.valueOf(name));

            // 添付画像を取得する
            BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();

            // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
            if (drawable != null) {
                Bitmap bitmap = drawable.getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                String bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                data.put("image", bitmapString);

            }

            //■◆■データをプッシュ！
            genreRef.push().setValue(data, this);
                                                                                                    Log.d("asat","■248■genreRef.push："+String.valueOf(data));//
                mProgress.show();
        }
    }

//■■■パーミッションダイアログによる許可再確認onRequestPermissionsResult
        @Override
        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            switch (requestCode) {                                                                      //onRequestPermissionsResultメソッド（パーミッションのリクエスト）ダイアログで確認
                case PERMISSIONS_REQUEST_CODE: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("asat","パーミッションの許可が押された");//━■━■━■━■━
                        // ユーザーが許可したとき
                        showChooser();
                    }
                    return;
                }
            }
        }

//■■■使用できるアプリを表示するためのメソッド
            private void showChooser() {  //---------------------------------------------------------------showChooseメソッド

                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);  //-------------------------ギャラリーから選択するIntentを生成
                galleryIntent.setType("image/*");  //------------------------------------------------------画像一般
                galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);  //----------------------------------必ずURIが受け渡されること

                String filename = System.currentTimeMillis() + ".jpg";  //---------------------------------カメラで撮影するIntent
                ContentValues values = new ContentValues();  //---------------------------------------------
                values.put(MediaStore.Images.Media.TITLE, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                mPictureUri = getContentResolver()
                        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  //-------------------カメラから選択するIntentを生成
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri);


                Intent chooserIntent = Intent.createChooser(galleryIntent, "画像を取得");//----------- 基本はギャラリーを選択するギャラリー選択のIntentを与えてcreateChooserメソッドを呼ぶ

                // EXTRA_INITIAL_INTENTS にカメラ撮影のIntentを追加  /
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});//------そのchooserにカメラのインテントを追加

                startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE);
            }

//■■■失敗したかどうかでスナックバーに判定
        @Override
        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {    //onCompleteだから成功しているかどうかの判定

            mProgress.dismiss();

            if (databaseError == null) {//-------------------------------------------------------------データベースが失敗じゃなかったら
                finish();  //---------------------------------------------------------------------------終わる
            } else {
                Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show();
            }  //---------------------------------------------------------------------------------------失敗の要素があればスナックバー
        }
}