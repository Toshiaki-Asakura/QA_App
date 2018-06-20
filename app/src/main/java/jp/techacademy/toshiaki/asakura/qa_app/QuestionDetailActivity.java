package jp.techacademy.toshiaki.asakura.qa_app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.google.firebase.auth.FirebaseAuth.getInstance;

public class QuestionDetailActivity extends AppCompatActivity{

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private DatabaseReference mAnswerRef;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {}

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

        @Override
        public void onCancelled(DatabaseError databaseError) {}
    };

//◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆onCreate◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        Log.d("asat","■82■mQuestion："+String.valueOf(mQuestion));

        setTitle(mQuestion.getTitle());

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
                    startActivity(intent);
                    // --- ここまで ---
                }
            }
        });


        FirebaseUser user = getInstance().getCurrentUser();

        if (user == null) {
            FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);  //---------------fabがログイン状態により可視不可状態
            fab2.setVisibility(View.INVISIBLE);
        } else {
            FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);  //---------------fabがログイン状態により可視化状態
            fab2.setVisibility(View.VISIBLE);

            fab2.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);

                    String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseUser user = getInstance().getCurrentUser();

//お気に入り質問を削除する（現在は判定できないため、ただ削除することを選択しています）
                    DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("getQuestionUid", mQuestion.getQuestionUid());
                    Log.d("asat","■138■mQuestion.getQuestionUid()："+mQuestion.getQuestionUid());
                    DatabaseReference favoriteRef = dataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                    favoriteRef.removeValue();
                    Log.d("asat","■141■favoriteRef："+String.valueOf(favoriteRef));
                    fab2.setBackgroundTintList(ColorStateList.valueOf(0xffff00ff));

//お気に入り質問を登録する
 /*                   DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("getQuestionUid", mQuestion.getQuestionUid());
                    data.put("name","asat");
                    Log.d("asat","■138■mQuestion.getQuestionUid()："+mQuestion.getQuestionUid());
                    DatabaseReference favoriteRef = dataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                    favoriteRef.push().setValue(data);
                    Log.d("asat","■141■favoriteRef："+String.valueOf(favoriteRef));
                    fab2.setBackgroundTintList(ColorStateList.valueOf(0xffff00ff));*/
                }
            });
        }

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);

    }

//◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆onCreate finish◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆
@Override
    protected void onResume() {  //----------------------------------------------------------------再描画的な感じなのか
    super.onResume();
    // 渡ってきたQuestionのオブジェクトを保持する
    Bundle extras = getIntent().getExtras();
    mQuestion = (Question) extras.get("question");

    setTitle(mQuestion.getTitle());

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
                startActivity(intent);
                // --- ここまで ---
            }
        }
    });

    DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
    mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
    mAnswerRef.addChildEventListener(mEventListener);
    }
}