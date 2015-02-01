package mavidser.willow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlayGame extends ActionBarActivity {
    String username,match;
    int level,CURRENT_QUESTION=-1,xp=0,opponent_xp=0,quesLeft=10,timeLeft=0;
    String ANS="";
    ProgressDialog pd;
    TextView score,opposcore;
    FragmentTransaction ft;
    FragmentImageQuestion iFragment = new FragmentImageQuestion();
    FragmentTextMCQuestion tFragment = new FragmentTextMCQuestion();
    GameResult rFragment = new GameResult();
    JSONArray obj;
    CountDownTimer CDtimer;


    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.150.1:4000/");
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        username = intent.getStringExtra("userid");
        level = intent.getIntExtra("level",1);

        mSocket.connect();
        System.out.println(username);
        mSocket.emit("connectUser", username);
//            mSocket.emit("questions", "test");
//            mSocket.on("message", response);
        getMatch();
        mSocket.on("receiveMatchingUser", matchReceived);
        mSocket.on("userDisconnected", userDisconnected);
        setContentView(R.layout.activity_play_game);
        score = (TextView) findViewById(R.id.play_user_score);
        opposcore = (TextView) findViewById(R.id.play_opponent_score);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        if (id == android.R.id.home) {
//            Intent upIntent = NavUtils.getParentActivityIntent(this);
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void getMatch() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("userid", username);
            obj.put("level", level);
        } catch (Exception e) {}
        mSocket.emit("matchUser", obj);

        pd=new ProgressDialog(this);

        pd.setIndeterminate(true);
        pd.setMessage(getString(R.string.waiting_for_a_match));
        pd.setCancelable(true);
        pd.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                onBackPressed();
                pd.cancel();
            }
        });

try {        pd.show();}catch (Exception e) {}
    }


    private Emitter.Listener oppoxp = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int xp = (int) args[0];
                    System.out.println("xp here " + xp);
                    opposcore.setText(xp + "");
                }
            });
        }
    };

    private Emitter.Listener matchReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String user = (String) args[0];
                    match = user;
                    TextView opponent = (TextView) findViewById(R.id.play_opponent);
                    TextView user_ = (TextView) findViewById(R.id.play_user);
                    user_.setText(username);
                    opponent.setText(match);
                    try {
                        pd.dismiss();
                    } catch (Exception e) {}
                }
            });
            mSocket.emit("questions", "test");
            mSocket.on("message", response);
            mSocket.on("oppoxp",oppoxp);
        }
    };

    private void startTheGame() {
        CDtimer = new CountDownTimer(6000, 100) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                timeLeft = (int)leftTimeInMilliseconds;
                TextView timer = (TextView) findViewById(R.id.timer);
                        timer.setText(""+(leftTimeInMilliseconds/1000));

            }
            @Override
            public void onFinish() {
                if(quesLeft>0) {
                    answer(null);
                    startTheGame();
                }
            }
        }.start();
    }

    private Emitter.Listener userDisconnected = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBackPressed();
                    Toast.makeText(getApplicationContext(), "Your opponent left the game",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
    private Emitter.Listener response = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    try {
                        String json = (String)args[0];
                        obj = new JSONArray(json);
                        startTheGame();
                        answer(null);
                    } catch (JSONException e) {
                        System.err.println(e);
                        return;
                    }
                }
            });
        }
    };

    public void putQuestionInFragment(int i) {
        System.out.println("HIHI");
        ft = getSupportFragmentManager().beginTransaction();

        try {
            obj.getJSONObject(i).getJSONObject("a").get("url");

            ft.replace(R.id.content_frame, iFragment);
        } catch (Exception e) {
            ft.replace(R.id.content_frame, tFragment);
        }
        ft.disallowAddToBackStack();
        ft.commit();
    }
//
    public void answer(View view) {


        String user_ans=null;
        if(view!=null) {


            System.out.println(ANS + " ANS");
            switch (view.getId()) {
                case R.id.text_a:
                case R.id.iai:
                    user_ans = "a";
                    break;
                case R.id.text_b:
                case R.id.ibi:
                    user_ans = "b";
                    break;
                case R.id.text_c:
                case R.id.ici:
                    user_ans = "c";
                    break;
                case R.id.text_d:
                case R.id.idi:
                    user_ans = "d";
                    break;
            }
            System.out.println(user_ans+" ans");

            if (ANS.equalsIgnoreCase(user_ans)) {
                xp += (int) ((timeLeft) * 50 / 6000);

            }
            score.setText(xp+"");

            JSONObject obj = new JSONObject();
            try {
                obj.put("id", username);
                obj.put("xp", xp);
            } catch (Exception e) {};
            mSocket.emit("answerPressed",obj);

            CDtimer.cancel();
            if(quesLeft>0)
                CDtimer.start();
        }


        Bundle data = new Bundle();
        try{
            ANS = obj.getJSONObject(++CURRENT_QUESTION).getString("ans");
            System.out.println(obj.getJSONObject(CURRENT_QUESTION).getString("ans")
             + " haha " + obj.getJSONObject(CURRENT_QUESTION).getString("q"));
            data.putString("question", obj.getJSONObject(CURRENT_QUESTION).getString("q")); data.putString("question", obj.getJSONObject(CURRENT_QUESTION).getString("q"));
            data.putString("ad", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("a").getString("desc"));
            data.putString("bd", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("b").getString("desc"));
            data.putString("cd", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("c").getString("desc"));
            data.putString("dd", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("d").getString("desc"));
            try {
                data.putString("ai", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("a").getString("url"));
                data.putString("bi", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("b").getString("url"));
                data.putString("ci", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("c").getString("url"));
                data.putString("di", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("d").getString("url"));
            }catch (Exception e){}


            try {
                obj.getJSONObject(CURRENT_QUESTION).getJSONObject("a").get("url");

                try {iFragment.setArguments(data);}
                catch(Exception ce){iFragment.setUIArguments(data);}

            } catch (Exception e) {

                try {tFragment.setArguments(data);}
                catch(Exception ce){tFragment.setUIArguments(data);}

            }
            putQuestionInFragment(CURRENT_QUESTION);
        } catch (Exception e) {
            System.err.println(e);

            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, rFragment);
            ft.disallowAddToBackStack();
            ft.commit();
        }

        quesLeft--;

    }

    @Override
    protected void onPause() {
        super.onPause();
        try{CDtimer.cancel();}catch (Exception e){}
        mSocket.disconnect();
        mSocket.off("receiveMatchingUser", matchReceived);
        mSocket.off("userDisconnected",userDisconnected);
        mSocket.off("message",response);
        mSocket.off("oppoxp",oppoxp);
        finish();
    }

    @Override
    public void onBackPressed() {
        System.out.println("back!");
        try {CDtimer.cancel();}
        catch(Exception e) {}
        finish();
    }
}
