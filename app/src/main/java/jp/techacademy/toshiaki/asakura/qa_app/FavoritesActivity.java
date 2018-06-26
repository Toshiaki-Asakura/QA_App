package jp.techacademy.toshiaki.asakura.qa_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import static com.google.firebase.auth.FirebaseAuth.getInstance;

public class FavoritesActivity extends AppCompatActivity {

    private int mGenre = 0;  //-------------------------------------------------------------------ジャンル選択時の数値、0からスタート    private DatabaseReference mContentsRef;
    private QuestionsListAdapter mAdapter;  //------------------------------------------------アダプターを呼び込むための変数
    private DatabaseReference mFavoriteRef;
    private DatabaseReference  mGenreCeckRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;  //--------------------------------------アレイリスト、クエスションのリストを参照するためのもの
    private ArrayList<String> mFavoriteArrayList;  //--------------------------------------アレイリスト、クエスションのリストを参照するためのもの
    private DatabaseReference mDatabaseReference;  //----------------------------------------データベースクラスの定義
    String mFavoriteQuestionUid;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        ArrayList<FavoritesActivity> mFavoriteQuestionUidList = new ArrayList<>();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.favo_listView);
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

        mQuestionArrayList.clear();
        mAdapter.setQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = getInstance().getCurrentUser();

        mFavoriteRef = mDatabaseReference.child(Const.FavoritesPATH).child(user.getUid());
        mFavoriteRef.addChildEventListener(mFavoriteListener);

        for (int i = 1; i <= 4; i++) {
            mGenreCeckRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(i));
            mGenreCeckRef.addChildEventListener(mGenreCeckListener);
        }
    }
}

