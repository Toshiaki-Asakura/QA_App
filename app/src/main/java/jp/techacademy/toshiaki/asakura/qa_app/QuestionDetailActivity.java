package jp.techacademy.toshiaki.asakura.qa_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import static com.google.firebase.auth.FirebaseAuth.getInstance;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private DatabaseReference mAnswerRef;
    private DatabaseReference mQuestionRef;

    //■
    private ChildEventListener mEventListener = new ChildEventListener() {
        SharedPreferences mPreference;

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for (Answer answer : mQuestion.getAnswers()) {
                Log.d("asat", "■46■answer：" + String.valueOf(answer));
                Log.d("asat", "■47■mQuestion：" + String.valueOf(mQuestion));
                Log.d("asat", "■48■mQuestion.getAnswers()：" + String.valueOf(mQuestion.getAnswers()));
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    Log.d("asat", "■51■answerUid：" + String.valueOf(answerUid));
                    Log.d("asat", "■52■answer.getAnswerUid()：" + String.valueOf(answer.getAnswerUid()));
                    return;
                }
            }
//■
            String body = (String) map.get("body");
            Log.d("asat", "■58■map.get(\"body\")：" + String.valueOf(map.get("body")));
            String name = (String) map.get("name");
            Log.d("asat", "■60■map.get(\"name\")：" + String.valueOf(map.get("name")));
            String uid = (String) map.get("uid");
            Log.d("asat", "■62■map.get(\"name\")：" + String.valueOf(map.get("name")));

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();

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


//◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆onCreate◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        Log.d("asat", "■99■mQuestion：" + String.valueOf(mQuestion));

        setTitle(mQuestion.getTitle());
        Log.d("asat", "■102■mQuestion.getTitle()：" + String.valueOf(mQuestion.getTitle()));
        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    // --- ここから ---
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    Log.d("asat", "■122■mQuestion：" + String.valueOf(mQuestion));
                    startActivity(intent);
                    // --- ここまで ---
                }
            }
        });


        FirebaseUser user = getInstance().getCurrentUser();

        if (user == null) {
            FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
            fab2.setVisibility(View.INVISIBLE);
        } else {
            FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
            fab2.setVisibility(View.VISIBLE);

            DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference favoriteRef = dataBaseReference.child(Const.FavoritesPATH).child(user.getUid());
            favoriteRef.addChildEventListener(mEventListener);

            ChildEventListener childEventListener = new ChildEventListener() {    //■■■赤 ChildEventListener
                FirebaseUser user = getInstance().getCurrentUser();

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    HashMap map = (HashMap) dataSnapshot.getValue();
                    String getQuestionUid = dataSnapshot.getKey();

                    FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
                    fab2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            for (Question question : user.getUid()) {   //■■■赤 user.getUid()
                                if (getQuestionUid.equals(user.getUid())) {  //■■■赤 getQuestionUid
                                    DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                                    FirebaseUser user = getInstance().getCurrentUser();
                                    Map<String, String> data = new HashMap<String, String>();
                                    data.put("getQuestionUid", mQuestion.getQuestionUid());
                                    DatabaseReference favoriteRef = dataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                                    favoriteRef.removeValue();
                                    FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
                                    fab2.setBackgroundTintList(ColorStateList.valueOf(0xff444444));
                                    return;
                                } else {
                                    DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                                    FirebaseUser user = getInstance().getCurrentUser();
                                    Map<String, String> data = new HashMap<String, String>();
                                    data.put("getQuestionUid", mQuestion.getQuestionUid());
                                    DatabaseReference favoriteRef = dataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                                    favoriteRef.push().setValue(data);
                                    FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
                                    fab2.setBackgroundTintList(ColorStateList.valueOf(0xffff00ff));
                                    return;
                                }
                            }
                        }
                    };  //■■■赤
            }
            };
        }
//▼
/*           DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference favoriteRef = dataBaseReference.child(Const.FavoritesPATH).child(user.getUid());
                                                                                                    Log.d("asat","■139■favoriteRef："+String.valueOf(favoriteRef));
            favoriteRef.addChildEventListener(mEventListener);

            FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);  //---------------fabがログイン状態により可視化状態
            fab2.setVisibility(View.VISIBLE);

            fab2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);

                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseUser user = getInstance().getCurrentUser();



                        int i = 1;
                        if (i == 0) {
//お気にを削除
                            DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                            Map<String, String> data = new HashMap<String, String>();
                            data.put("getQuestionUid", mQuestion.getQuestionUid());
                                                                                                    Log.d("asat", "■163■mQuestion.getQuestionUid()：" + mQuestion.getQuestionUid());
                            DatabaseReference favoriteRef = dataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                            favoriteRef.removeValue();
                                                                                                    Log.d("asat", "■166■favoriteRef：" + String.valueOf(favoriteRef));
                            fab2.setBackgroundTintList(ColorStateList.valueOf(0xffff00ff));
                        } else {
//お気にを登録
                            DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                            Map<String, String> data = new HashMap<String, String>();
                            data.put("getQuestionUid", mQuestion.getQuestionUid());
                                                                                                    Log.d("asat", "■173■mQuestion.getQuestionUid()：" + mQuestion.getQuestionUid());
                            DatabaseReference favoriteRef = dataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                            favoriteRef.push().setValue(data);
                                                                                                    Log.d("asat", "■176■favoriteRef：" + String.valueOf(favoriteRef));
                            fab2.setBackgroundTintList(ColorStateList.valueOf(0xffff00ff));
                        }
                }
            });
        }*/

            DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
            mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
            mAnswerRef.addChildEventListener(mEventListener);

        }

//◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆onCreate finish◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆

    }
}