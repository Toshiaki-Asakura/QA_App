package jp.techacademy.toshiaki.asakura.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;  //-----------------------------------------------------------------ツールバーのメンバ変数
    private int mGenre = 0;  //-------------------------------------------------------------------ジャンル選択時の数値、0からスタート
    private DatabaseReference mDatabaseReference;  //----------------------------------------データベースクラスの定義
    private DatabaseReference mGenreRef;  //--------------------------------------------------ジャンルごとの階層にアクセスするための変数
    private ListView mListView;  //-----------------------------------------------------------リストビュー
    private ArrayList<Question> mQuestionArrayList;  //--------------------------------------アレイリスト、クエスションのリストを参照するためのもの
    private ArrayList<String> mFavoriteArrayList;  //--------------------------------------アレイリスト、クエスションのリストを参照するためのもの
    private QuestionsListAdapter mAdapter;  //------------------------------------------------アダプターを呼び込むための変数
    private DatabaseReference mFavoriteRef;
    private DatabaseReference  mGenreCeckRef;
    String mFavoriteQuestionUid;

    private ChildEventListener mEventListener = new ChildEventListener() {  //---------------データベースのアイテムを取り出す
            @Override//
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {  //--------------------データベースの全体像（DataSnapshot）の各addにアクセス

                HashMap map = (HashMap) dataSnapshot.getValue();  //--------------------------------DataSnapshotに「map」という名をつけて
                String title = (String) map.get("title");
                String body = (String) map.get("body");
                String name = (String) map.get("name");  //                                         5つのデータをセットします
                String uid = (String) map.get("uid");
                String imageString = (String) map.get("image");

                byte[] bytes;
                if (imageString != null) {
                    bytes = Base64.decode(imageString, Base64.DEFAULT);  //------------------------imageStringがnullじゃなければデフォルトを使い
                } else {
                    bytes = new byte[0];  //--------------------------------------------------------nullだったらbyteをゼロに　なんだこれ
                }

                ArrayList<Answer> answerArrayList = new ArrayList<Answer>();  //--------------------Answerクラスのアレイリストを生成します
                HashMap answerMap = (HashMap) map.get("answers");
                if (answerMap != null) {
                    for (Object key : answerMap.keySet()) {
                        HashMap temp = (HashMap) answerMap.get((String) key);
                        String answerBody = (String) temp.get("body");
                        String answerName = (String) temp.get("name"); //                          answer付きの5つのデータをセットします
                        String answerUid = (String) temp.get("uid");

                        Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                        answerArrayList.add(answer);
                                                                                                    Log.d("asat","▼answerArrayList▼："+answerArrayList.size());
                    }
                }
                Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
                mQuestionArrayList.add(question);


                                                                                                    Log.d("asat","■mQuestionArrayList■："+mQuestionArrayList.size());
                mAdapter.notifyDataSetChanged();
            }
            @Override//■■■DataSnapshotの要素に変化があった時に呼ばれます。
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                HashMap map = (HashMap) dataSnapshot.getValue();

                // 変更があったQuestionを探す
                for (Question question: mQuestionArrayList) {
                    if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                        // このアプリで変更がある可能性があるのは回答(Answer)のみ
                        question.getAnswers().clear();
                        HashMap answerMap = (HashMap) map.get("answers");
                        if (answerMap != null) {
                            for (Object key : answerMap.keySet()) {
                                HashMap temp = (HashMap) answerMap.get((String) key);
                                String answerBody = (String) temp.get("body");
                                String answerName = (String) temp.get("name");
                                String answerUid = (String) temp.get("uid");
                                Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                                question.getAnswers().add(answer);
                                                                                                    Log.d("asat","▲question▲："+question.getAnswers().size());
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
           @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
    private ChildEventListener mFavoriteListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();
            String mFavoriteQuestionUid =dataSnapshot.getKey();
            mFavoriteArrayList.add(mFavoriteQuestionUid);
        }
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {



        }
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {



        }
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {



        }
        @Override
        public void onCancelled(DatabaseError databaseError) {



        }
    };
    private ChildEventListener mGenreCeckListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();
            String title = (String) map.get("title");
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");
            String imageString = (String) map.get("image");
            byte[] bytes;
            if (imageString != null) {
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }
            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap) map.get("answers");
            if (answerMap != null) {
                for (Object key : answerMap.keySet()) {
                    HashMap temp = (HashMap) answerMap.get((String) key);
                    String answerBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                    answerArrayList.add(answer);
                }
            }
            Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
            if(mFavoriteQuestionUid.contains(dataSnapshot.getKey())){
                mQuestionArrayList.add(question);
            }
            mAdapter.notifyDataSetChanged();
        }
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // 変更があったQuestionを探す
            for (Question question: mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
//◆◆◆◆◆◆onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {  //--------------------------------------描画開始
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  //-----------------------------------------------activity_mainの画面描画
                                                                                                    Log.d("asat","MainActivity onCreate開始");//

        mToolbar = (Toolbar) findViewById(R.id.toolbar);  //--------------------------------------M変数mToolberのfindViewById設定
        setSupportActionBar(mToolbar);  //--------------------------------------------------------setSupportActionBarをmToolbarに

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);  //---------------fabの設定
        fab.setOnClickListener(new View.OnClickListener() {  //-------------------------------------匿名クラスでonClick

            @Override
                public void onClick(View view) {

                if (mGenre == 0) {  //-----------------------------------------------------ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
                    Snackbar.make(view, "お気に入りには投稿出来ません", Snackbar.LENGTH_LONG).show();
                    return;
                }

                 FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();  //----------------getCurrentUserでログイン状態かどうかわかる
                //--------ログインしていない場合はgetCurrentUserはnullを返す
                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {//--------------------------------------------------------------------------// ジャンルを渡して質問作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                    intent.putExtra("genre", mGenre);  //----------------------------------"genre"=mGenreと併せてインテント
                    startActivity(intent);
                }
            }
        });  //-------------------------------------------------------------------------------------リスナのくくり});


        // ナビゲーションドロワーの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);  //----------------「drawer」というレイアウトの定義activity_mainにあり、
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name); //これが分からん
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);  //----------「navigationView」というUI部品の定義activity_mainにあり、
        navigationView.setNavigationItemSelectedListener(this);  //---------------------------------リスナー実装
        Log.d("asat","ナヴィゲーションドロワー準備完了");//

        Menu menu = navigationView.getMenu();
        MenuItem item = menu.findItem(R.id.nav_favo);

        // ログイン済みのユーザーを取得する
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // ログインしていなければお気に入りを非表示にする
            item.setVisible(false);
        }else{
            // ログインしていればお気に入りを表示する
            item.setVisible(true);
        }

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });
    }
//◆◆◆◆◆◆onCreate finish
    @Override
    protected void onResume() {  //----------------------------------------------------------------再描画的な感じなのか
        super.onResume();
        if(mGenre == 0) {
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            onNavigationItemSelected(navigationView.getMenu().getItem(1));
        }
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        MenuItem item = menu.findItem(R.id.nav_favo);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            item.setVisible(false);
        }else{
            item.setVisible(true);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {  //--------------------------------------------オプションメニューを呼び出すんだろう
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);  //-------------------------------------UI部品ではなくxlmファイルmenu.menu_mainのmenuの中身を見るようだ
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {  //--------------------------------------オプションアイテムを選択したかどうか
        int id = item.getItemId();  //--------------------------------------------------------------⇒true（押されたら）ならidをitem.getItemIdに

        if (id == R.id.action_settings) {  //-----------------------------------------------------右上んぽボタンでセッティング画面を開く⇒名前変更・ログアウト
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);  //----------------------------------------------オプションアイテムを選択されなかったら消える
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {  //----------------------------------onNavigationItemSelectedが選択されたかどうかを判定
        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mQuestionArrayList.clear();
        mAdapter.setQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);

        // 選択したジャンルにリスナーを登録する
        if (mGenreRef != null) {
            mGenreRef.removeEventListener(mEventListener);
        }
                                                                                                    /*        mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
                                                                                                            mGenreRef.addChildEventListener(mEventListener);*/
        int id = item.getItemId();  //--------------------------------------------------------------⇒true（押されたら）ならidをitem.getItemIdに

        if (id == R.id.nav_hobby) {  //------------------------------------------------------------R.id.nav_hobby
            mToolbar.setTitle("趣味");  //--------------------------------------------------------タイトル「趣味」
            mGenre = 1;  //------------------------------------------------------------------------後で何かに使う数字を1(int)
        } else if (id == R.id.nav_life) {  //------------------------------------------------------id == R.id.nav_life
            mToolbar.setTitle("生活");  //--------------------------------------------------------タイトル「生活」
            mGenre = 2;  //------------------------------------------------------------------------後で何かに使う数字を2(int)
        } else if (id == R.id.nav_health) {  //---------------------------------------------------id == R.id.nav_health
            mToolbar.setTitle("健康");  //--------------------------------------------------------「健康」
            mGenre = 3;  //------------------------------------------------------------------------後で何かに使う数字を3(int)
        } else if (id == R.id.nav_compter) {  //--------------------------------------------------id == R.id.nav_compter
            mToolbar.setTitle("コンピューター");  //----------------------------------------------「コンピューター」
            mGenre = 4;  //------------------------------------------------------------------------後で何かに使う数字を4(int)
        } else if (id == R.id.nav_favo) {  //-----------------------------------------------------id == R.id.nav_favo
            mToolbar.setTitle("お気に入り");  //----------------------------------------------「お気に入り」
            mGenre = 0;  //------------------------------------------------------------------------後で何かに使う数字を4(int)
        }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
            mQuestionArrayList.clear();
            mAdapter.setQuestionArrayList(mQuestionArrayList);
            mListView.setAdapter(mAdapter);

            // 選択したジャンルにリスナーを登録する
            if (mGenreRef != null) {
                mGenreRef.removeEventListener(mEventListener);
            }

            if(mGenre==0) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();  //----------------getCurrentUserでログイン状態かどうかわかる
                mFavoriteRef = mDatabaseReference.child(Const.FavoritesPATH).child(user.getUid());
                mFavoriteRef.addChildEventListener(mFavoriteListener);

                for (int i = 1; i <= 4; i++) {
                    mGenreCeckRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(i));
                    mGenreCeckRef.addChildEventListener(mGenreCeckListener);
                }
            }else{
                    mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
                    mGenreRef.addChildEventListener(mEventListener);
            }

        return true;  //---------------------------------------------------------------------------
    }
}