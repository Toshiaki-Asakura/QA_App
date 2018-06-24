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
    private DatabaseReference mFavoriteRef;
    private boolean mFavoriteFlag =false;
    private int mGenre;


    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();
            String answerUid = dataSnapshot.getKey();
            for (Answer answer : mQuestion.getAnswers()) {
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

    private ChildEventListener mFavoriteListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            mFavoriteFlag =true;
            FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
            if (mFavoriteFlag  == true) {
                fab2.setBackgroundTintList(ColorStateList.valueOf(0xffff00ff));
            } else {
                fab2.setBackgroundTintList(ColorStateList.valueOf(0xff888888));
            }
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
//--------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

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
        if(user==null){
            FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);  //---------------fabがログイン状態により可視化状態
            fab2.setVisibility(View.INVISIBLE);
        }else{
            FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);  //---------------fabがログイン状態により可視化状態
            fab2.setVisibility(View.VISIBLE);
        }

        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                 FirebaseUser user = getInstance().getCurrentUser();
                DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                Map<String, String> data = new HashMap<String, String>();
                data.put("QuestionUid", mQuestion.getQuestionUid());
                DatabaseReference favoriteRef = dataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                String questionid = mQuestion.getQuestionUid();
                data.put("mGenre", String.valueOf(mQuestion.getGenre()));
                DatabaseReference contentsRef =  dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre()));
                String contentsid = String.valueOf(mQuestion.getGenre());
                                                                                                    Log.d("asat","■uid■："+String.valueOf(uid));
                                                                                                    Log.d("asat","■mQuestion■："+String.valueOf(mQuestion));
                                                                                                    Log.d("asat","■favoriteRef■："+String.valueOf(favoriteRef));
                                                                                                    Log.d("asat","■questionid■："+String.valueOf(questionid));
                                                                                                    Log.d("asat","■contentsRef■："+String.valueOf(contentsRef));
                                                                                                    Log.d("asat","■contentsid■："+String.valueOf(contentsid));
                FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
                if (mFavoriteFlag  == true) {
                    favoriteRef.removeValue();
                    fab2.setBackgroundTintList(ColorStateList.valueOf(0xff888888));//お気に入り削除で「グレー」
                    mFavoriteFlag=false;
                } else {
                    favoriteRef.setValue(data);
                    fab2.setBackgroundTintList(ColorStateList.valueOf(0xffff00ff));//お気に入り登録で「ピンク」
                    mFavoriteFlag=true;

                }
            }
        });

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);

        if(user!=null) {
            mFavoriteRef = dataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
            mFavoriteRef.addChildEventListener(mFavoriteListener);
        } else { return; }

    }
//--------------------------------------------------------------------------------------------------
}